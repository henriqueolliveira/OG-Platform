/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.time.Instant;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFilter;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.LiveDataSourcingFunction;
import com.opengamma.engine.function.YieldCurveDataSourcingFunction;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.InMemoryViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessContext;
import com.opengamma.engine.view.cache.CacheSelectHint;
import com.opengamma.engine.view.cache.MissingLiveDataSentinel;
import com.opengamma.engine.view.cache.ViewComputationCache;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGatherer;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Holds all data and actions for a single computation pass. The view cycle may be executed at most once.
 * <p>
 * The cycle is thread-safe for readers, for example obtaining the current state or the result, but is only designed
 * for a single executor.
 */
public class SingleComputationCycle implements ViewCycle, EngineResource {
  private static final Logger s_logger = LoggerFactory.getLogger(SingleComputationCycle.class);
  
  // Injected inputs
  private final UniqueIdentifier _cycleId;
  private final UniqueIdentifier _viewProcessId;
  private final ViewProcessContext _viewProcessContext;
  private final CompiledViewDefinitionWithGraphsImpl _compiledViewDefinition;
  private final ViewCycleExecutionOptions _executionOptions;
  private final ViewExecutionOptions _jobExecutingOptions;

  private final DependencyGraphExecutor<?> _dependencyGraphExecutor;
  private final GraphExecutorStatisticsGatherer _statisticsGatherer;
  private final LiveDataSnapshotProvider _liveDataSnapshotProvider;

  private volatile ViewCycleState _state = ViewCycleState.AWAITING_EXECUTION;

  /**
   * Nanoseconds, see System.nanoTime()
   */
  private volatile long _startTime;

  /**
   * Nanoseconds, see System.nanoTime()
   */
  private volatile long _endTime;

  private final ReentrantReadWriteLock _nodeExecutionLock = new ReentrantReadWriteLock();
  private final Set<DependencyNode> _executedNodes = new HashSet<DependencyNode>();
  private final Set<DependencyNode> _failedNodes = new HashSet<DependencyNode>();
  private final Map<String, ViewComputationCache> _cachesByCalculationConfiguration = new HashMap<String, ViewComputationCache>();

  // Output
  private final InMemoryViewComputationResultModel _resultModel;

  public SingleComputationCycle(UniqueIdentifier cycleId, UniqueIdentifier viewProcessId,
      ViewProcessContext viewProcessContext, CompiledViewDefinitionWithGraphsImpl compiledViewDefinition,
      ViewCycleExecutionOptions executionOptions, ViewExecutionOptions jobExecutingOptions) {
    ArgumentChecker.notNull(viewProcessContext, "viewProcessContext");
    ArgumentChecker.notNull(compiledViewDefinition, "compiledViewDefinition");

    _cycleId = cycleId;
    _viewProcessId = viewProcessId;
    _viewProcessContext = viewProcessContext;
    _compiledViewDefinition = compiledViewDefinition;
    
    _executionOptions = executionOptions;
    _jobExecutingOptions = jobExecutingOptions;

    _resultModel = new InMemoryViewComputationResultModel();
    _resultModel.setCalculationConfigurationNames(getCompiledViewDefinition().getViewDefinition().getAllCalculationConfigurationNames());
    if (getCompiledViewDefinition().getPortfolio() != null) {
      _resultModel.setPortfolio(getCompiledViewDefinition().getPortfolio());
    }
    _resultModel.setViewCycleId(cycleId);
    _resultModel.setViewProcessId(getViewProcessId());
    _resultModel.setValuationTime(executionOptions.getValuationTime());

    _dependencyGraphExecutor = getViewProcessContext().getDependencyGraphExecutorFactory().createExecutor(this);
    _statisticsGatherer = getViewProcessContext().getGraphExecutorStatisticsGathererProvider().getStatisticsGatherer(getViewProcessId());
    _liveDataSnapshotProvider = getViewProcessContext().getLiveDataSnapshotProvider(_jobExecutingOptions);
  }
  
  //-------------------------------------------------------------------------
  public Instant getValuationTime() {
    return _executionOptions.getValuationTime();
  }
  
  public Instant getInputDataTime() {
    return _executionOptions.getInputDataTime();
  }

  public long getFunctionInitId() {
    return getCompiledViewDefinition().getFunctionInitId();
  }

  /**
   * @return the start time. Nanoseconds, see {@link System#nanoTime()}. 
   */
  public long getStartTime() {
    return _startTime;
  }

  /**
   * @return the end time. Nanoseconds, see {@link System#nanoTime()}. 
   */
  public long getEndTime() {
    return _endTime;
  }

  /**
   * @return the viewDefinition
   */
  public ViewDefinition getViewDefinition() {
    return getCompiledViewDefinition().getViewDefinition();
  }

  public DependencyGraphExecutor<?> getDependencyGraphExecutor() {
    return _dependencyGraphExecutor;
  }

  public GraphExecutorStatisticsGatherer getStatisticsGatherer() {
    return _statisticsGatherer;
  } 

  public Map<String, ViewComputationCache> getCachesByCalculationConfiguration() {
    return Collections.unmodifiableMap(_cachesByCalculationConfiguration);
  }
  
  public ViewProcessContext getViewProcessContext() {
    return _viewProcessContext;
  }

  public Set<String> getAllCalculationConfigurationNames() {
    return new HashSet<String>(getCompiledViewDefinition().getViewDefinition().getAllCalculationConfigurationNames());
  }
  
  //-------------------------------------------------------------------------
  @Override
  public UniqueIdentifier getUniqueId() {
    return _cycleId;
  }

  @Override
  public UniqueIdentifier getViewProcessId() {
    return _viewProcessId;
  }
  
  @Override
  public ViewCycleState getState() {
    return _state;
  }

  
  @Override
  public long getDurationNanos() {
    ViewCycleState state = getState();
    if (state == ViewCycleState.AWAITING_EXECUTION || state == ViewCycleState.EXECUTION_INTERRUPTED) {
      return -1;
    }
    long startTime = getStartTime();
    long endTime = getEndTime();
    return endTime == 0 ? System.nanoTime() - startTime : endTime - startTime;
  }
  
  @Override
  public CompiledViewDefinitionWithGraphsImpl getCompiledViewDefinition() {
    return _compiledViewDefinition;
  }
  
  @Override
  public InMemoryViewComputationResultModel getResultModel() {
    return _resultModel;
  }
  
  @Override
  public ComputationCacheResponse queryComputationCaches(ComputationCacheQuery query) {
    ArgumentChecker.notNull(query, "query");
    ArgumentChecker.notNull(query.getCalculationConfigurationName(), "calculationConfigurationName");
    ArgumentChecker.notNull(query.getValueSpecifications(), "valueSpecifications");
    
    ViewComputationCache cache = getComputationCache(query.getCalculationConfigurationName());
    if (cache == null) {
      throw new DataNotFoundException("No computation cache for calculation configuration '" + query.getCalculationConfigurationName()
          + "' was found.");
    }
    
    Collection<Pair<ValueSpecification, Object>> result = cache.getValues(query.getValueSpecifications());
    
    ComputationCacheResponse response = new ComputationCacheResponse();
    response.setResults(result);
    return response;
  }

  //--------------------------------------------------------------------------  
  // REVIEW jonathan 2011-03-18 -- The following comment should be given some sort of 'listed' status for preservation :-)
  // REVIEW kirk 2009-11-03 -- This is a database kernel. Act accordingly.
  /**
   * Synchronously runs the cycle.
   * 
   * @param previousCycle  the previous cycle from which a delta cycle should be performed, or {@code null} to perform
   *                       a full cycle
   * @throws InterruptedException  if the thread is interrupted while waiting for the computation cycle to complete.
   *                               Execution of any outstanding jobs will be cancelled, but {@link #release()}
   *                               still must be called.
   */
  public void execute(SingleComputationCycle previousCycle) throws InterruptedException {    
    if (_state != ViewCycleState.AWAITING_EXECUTION) {
      throw new IllegalStateException("State must be " + ViewCycleState.AWAITING_EXECUTION);
    }
    _startTime = System.nanoTime();
    _state = ViewCycleState.EXECUTING;

    createAllCaches();    
    prepareInputs();
    
    if (previousCycle != null) {
      computeDelta(previousCycle);
    }

    LinkedList<Future<?>> futures = new LinkedList<Future<?>>();

    for (String calcConfigurationName : getAllCalculationConfigurationNames()) {
      s_logger.info("Executing plans for calculation configuration {}", calcConfigurationName);
      DependencyGraph depGraph = getExecutableDependencyGraph(calcConfigurationName);

      s_logger.info("Submitting {} for execution by {}", depGraph, getDependencyGraphExecutor());

      Future<?> future = getDependencyGraphExecutor().execute(depGraph, _statisticsGatherer);
      futures.add(future);
    }

    while (!futures.isEmpty()) {
      Future<?> future = futures.poll();
      try {
        future.get(5, TimeUnit.SECONDS);
      } catch (TimeoutException e) {
        s_logger.info("Waiting for " + future);
        futures.add(future);
      } catch (InterruptedException e) {
        Thread.interrupted();
        // Cancel all outstanding jobs to free up resources
        future.cancel(true);
        for (Future<?> incompleteFuture : futures) {
          incompleteFuture.cancel(true);
        }
        _state = ViewCycleState.EXECUTION_INTERRUPTED;
        s_logger.info("Execution interrupted before completion.");
        throw e;
      } catch (ExecutionException e) {
        s_logger.error("Unable to execute dependency graph", e);
        // Should we be swallowing this or not?
        throw new OpenGammaRuntimeException("Unable to execute dependency graph", e);
      }
    }

    populateResultModel();
    
    _state = ViewCycleState.EXECUTED;
    _endTime = System.nanoTime();
  }
 
  //-------------------------------------------------------------------------
  private void prepareInputs() {
    Map<ValueRequirement, ValueSpecification> allLiveDataRequirements = getCompiledViewDefinition().getLiveDataRequirements();
    s_logger.debug("Populating {} market data items for snapshot {}", allLiveDataRequirements.size(), getValuationTime());

    _liveDataSnapshotProvider.snapshot(getInputDataTime().toEpochMillisLong());

    Set<ValueSpecification> missingLiveData = new HashSet<ValueSpecification>();

    if (_liveDataSnapshotProvider.hasStructuredData()) {
      for (Map.Entry<YieldCurveKey, ValueSpecification> yieldCurveReq : getYieldCurveDataRequirements().entrySet()) {
        SnapshotDataBundle bundle = _liveDataSnapshotProvider.querySnapshot(getValuationTime().toEpochMillisLong(),
            yieldCurveReq.getKey());
        if (bundle == null) {
          throw new NotImplementedException("Should use unstructured data here");
        }
        if (bundle.getDataPoints() == null) { // TODO duplicate below
          s_logger.debug("Unable to load yield curve value for {} at snapshot {}.", yieldCurveReq.getKey(),
              getValuationTime());
          missingLiveData.add(yieldCurveReq.getValue());
        } else {
          ComputedValue dataAsValue = new ComputedValue(yieldCurveReq.getValue(), bundle);
          addToAllCaches(dataAsValue);
          getResultModel().addLiveData(dataAsValue);
        }
      }
    }

    for (Map.Entry<ValueRequirement, ValueSpecification> liveDataRequirement : allLiveDataRequirements.entrySet()) {
      // REVIEW 2010-10-22 Andrew
      // If we're asking the snapshot for a "requirement" then it should give back a more detailed "specification" with the data (i.e. a
      // ComputedValue instance where the specification satisfies the requirement. Functions should then declare their requirements and
      // not the exact specification they want for live data. Alternatively, if the snapshot will give us the exact value we ask for then
      // we should be querying with a "specification" and not a requirement.
      Object data = _liveDataSnapshotProvider.querySnapshot(getValuationTime().toEpochMillisLong(), liveDataRequirement.getKey());
      ComputedValue dataAsValue;
      
      if (data == null) {
        s_logger.debug("Unable to load live data value for {} at snapshot {}.", liveDataRequirement, getValuationTime());
        missingLiveData.add(liveDataRequirement.getValue());
        dataAsValue = new ComputedValue(liveDataRequirement.getValue(), MissingLiveDataSentinel.getInstance());
      } else {
        dataAsValue = new ComputedValue(liveDataRequirement.getValue(), data);
        getResultModel().addLiveData(dataAsValue);
      }
      addToAllCaches(dataAsValue);
    }
    if (!missingLiveData.isEmpty()) {
      s_logger.warn("Missing {} live data elements: {}", missingLiveData.size(), formatMissingLiveData(missingLiveData));
    }
  }

  private Map<YieldCurveKey, ValueSpecification> getYieldCurveDataRequirements() {
    Map<YieldCurveKey, ValueSpecification> ret = new HashMap<YieldCurveKey, ValueSpecification>();

    for (String calcConfigurationName : getAllCalculationConfigurationNames()) {
      Map<YieldCurveKey, ValueSpecification> configReqs = processYieldCurveDataRequirements(getDependencyGraph(calcConfigurationName));
      ret.putAll(configReqs);
    }
    return ret;
  }

  /**
   * TODO: should this be in CompiledViewCalculationConfiguration like the unstructured data
   * @param dependencyGraph
   * @return
   */
  private static Map<YieldCurveKey, ValueSpecification> processYieldCurveDataRequirements(
      DependencyGraph dependencyGraph) {
    ArgumentChecker.notNull(dependencyGraph, "dependencyGraph");
    HashMap<YieldCurveKey, ValueSpecification> ret = new HashMap<YieldCurveKey, ValueSpecification>();

    Set<DependencyNode> dependencyNodes = dependencyGraph.getDependencyNodes();
    for (DependencyNode dependencyNode : dependencyNodes) {
      CompiledFunctionDefinition compiledFunction = dependencyNode.getFunction().getFunction();
      if (compiledFunction instanceof YieldCurveDataSourcingFunction) {
        YieldCurveDataSourcingFunction function = (YieldCurveDataSourcingFunction) compiledFunction;
        Set<YieldCurveKey> yieldCurveKeys = function.getYieldCurveKeys();
        for (YieldCurveKey yieldCurveKey : yieldCurveKeys) {
          ret.put(yieldCurveKey, getYieldCurveDataSpec(compiledFunction, yieldCurveKey));
        }
      }
    }
    return ret;
  }

  private static ValueSpecification getYieldCurveDataSpec(CompiledFunctionDefinition compiledFunction,
      YieldCurveKey yieldCurveKey) {
    String uniqueId = compiledFunction.getFunctionDefinition().getUniqueId();
    ValueSpecification spec = new ValueSpecification(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, new ComputationTargetSpecification(yieldCurveKey.getCurrency()),
        ValueProperties.with(ValuePropertyNames.FUNCTION, uniqueId).with(ValuePropertyNames.CURVE, yieldCurveKey.getName()).get());
    return spec;
  }

  private static String formatMissingLiveData(Set<ValueSpecification> missingLiveData) {
    StringBuilder sb = new StringBuilder();
    for (ValueSpecification spec : missingLiveData) {
      sb.append("[").append(spec.getValueName()).append(" on ");
      sb.append(spec.getTargetSpecification().getType());
      if (spec.getTargetSpecification().getType() == ComputationTargetType.PRIMITIVE) {
        sb.append("-").append(spec.getTargetSpecification().getIdentifier().getScheme().getName());
      }
      sb.append(":").append(spec.getTargetSpecification().getIdentifier().getValue()).append("] ");
    }
    return sb.toString();
  }
  
  /**
   * 
   */
  private void createAllCaches() {
    for (String calcConfigurationName : getAllCalculationConfigurationNames()) {
      ViewComputationCache cache = getViewProcessContext().getComputationCacheSource().getCache(
          getViewProcessId(), calcConfigurationName, getValuationTime().toEpochMillisLong());
      _cachesByCalculationConfiguration.put(calcConfigurationName, cache);
    }
  }

  /**
   * @param dataAsValue
   */
  private void addToAllCaches(ComputedValue dataAsValue) {
    for (String calcConfigurationName : getAllCalculationConfigurationNames()) {
      getComputationCache(calcConfigurationName).putSharedValue(dataAsValue);
    }
  }
  
  private ViewComputationCache getComputationCache(String calcConfigName) {
    return _cachesByCalculationConfiguration.get(calcConfigName);
  }
  
  /**
   * Determine which live data inputs have changed between iterations, and:
   * <ul>
   * <li>Copy over all values that can be demonstrated to be the same from the previous iteration (because no input has changed)
   * <li>Only recompute the values that could have changed based on live data inputs
   * </ul> 
   * 
   * @param previousCycle Previous iteration. It must not have been cleaned yet ({@link #releaseResources()}).
   */
  private void computeDelta(SingleComputationCycle previousCycle) {
    if (previousCycle.getState() != ViewCycleState.EXECUTED) {
      throw new IllegalArgumentException("State of previous cycle must be " + ViewCycleState.EXECUTED);
    }

    for (String calcConfigurationName : getAllCalculationConfigurationNames()) {
      DependencyGraph depGraph = getCompiledViewDefinition().getDependencyGraph(calcConfigurationName);

      ViewComputationCache cache = getComputationCache(calcConfigurationName);
      ViewComputationCache previousCache = previousCycle.getComputationCache(calcConfigurationName);

      LiveDataDeltaCalculator deltaCalculator = new LiveDataDeltaCalculator(depGraph, cache, previousCache);
      deltaCalculator.computeDelta();

      s_logger.info("Computed delta for calculation configuration '{}'. {} nodes out of {} require recomputation.",
          new Object[] {calcConfigurationName, deltaCalculator.getChangedNodes().size(), depGraph.getSize()});

      Collection<ValueSpecification> specsToCopy = new HashSet<ValueSpecification>();
      
      for (DependencyNode unchangedNode : deltaCalculator.getUnchangedNodes()) {
        markExecuted(unchangedNode);
        specsToCopy.addAll(unchangedNode.getOutputValues());
      }
      
      copyValues(cache, previousCache, specsToCopy);
    }
  }

  private void copyValues(ViewComputationCache cache, ViewComputationCache previousCache, Collection<ValueSpecification> specsToCopy) {
    Collection<Pair<ValueSpecification, Object>> valuesToCopy = previousCache.getValues(specsToCopy);
    
    Collection<ComputedValue> newValues = new HashSet<ComputedValue>();
    for (Pair<ValueSpecification, Object> pair : valuesToCopy) {
      Object previousValue = pair.getSecond();
      if (previousValue != null) {
        newValues.add(new ComputedValue(pair.getFirst(), previousValue));
      }
    }
    cache.putSharedValues(newValues);
  }
  
  private void populateResultModel() {
    getResultModel().setResultTimestamp(Instant.now());
    for (String calcConfigurationName : getAllCalculationConfigurationNames()) {
      DependencyGraph depGraph = getCompiledViewDefinition().getDependencyGraph(calcConfigurationName);
      populateResultModel(calcConfigurationName, depGraph);
    }
  }

  private void populateResultModel(String calcConfigurationName, DependencyGraph depGraph) {
    ViewComputationCache computationCache = getComputationCache(calcConfigurationName);
    for (Pair<ValueSpecification, Object> value : computationCache.getValues(depGraph.getOutputSpecifications(), CacheSelectHint.allShared())) {
      if (value.getValue() == null) {
        continue;
      }
      if (!getViewDefinition().getResultModelDefinition().shouldOutputResult(value.getFirst(), depGraph)) {
        continue;
      }
      if (value.getSecond() instanceof MissingLiveDataSentinel) {
        continue;
      }
      getResultModel().addValue(calcConfigurationName, new ComputedValue(value.getFirst(), value.getSecond()));
    }
  }
  
  private DependencyGraph getDependencyGraph(String calcConfName) {
    DependencyGraph depGraph = getCompiledViewDefinition().getDependencyGraph(calcConfName);
    return depGraph;
  }

  /**
   * @param calcConfName configuration name
   * @return A dependency graph with nodes already executed stripped out.
   * See {@link #computeDelta} and how it calls {@link #markExecuted}.
   */
  protected DependencyGraph getExecutableDependencyGraph(String calcConfName) {
    DependencyGraph originalDepGraph = getDependencyGraph(calcConfName);

    final boolean haveResolvedStructuredData = _liveDataSnapshotProvider.hasStructuredData();

    DependencyGraph dependencyGraph = originalDepGraph.subGraph(new DependencyNodeFilter() {
      public boolean accept(DependencyNode node) {
        // LiveData functions do not need to be computed.
        if (node.getFunction().getFunction() instanceof LiveDataSourcingFunction) {
          markExecuted(node);
        }
        if (haveResolvedStructuredData && node.getFunction().getFunction() instanceof YieldCurveDataSourcingFunction) {
          markExecuted(node);
        }

        return !isExecuted(node);
      }
    });
    return dependencyGraph;
  }

  //--------------------------------------------------------------------------
  @Override
  public void release() {
    if (getState() == ViewCycleState.DESTROYED) {
      throw new IllegalStateException("View cycle " + getUniqueId() +  " has already been released");
    }
    
    if (getViewDefinition().isDumpComputationCacheToDisk()) {
      dumpComputationCachesToDisk();
    }

    // [PLAT-1124] BUG - what if 2 cycles use the same snapshot provider with the same evaluation time?
    _liveDataSnapshotProvider.releaseSnapshot(getValuationTime().toEpochMillisLong());
    getViewProcessContext().getComputationCacheSource().releaseCaches(getViewProcessId(), getValuationTime().toEpochMillisLong());

    _state = ViewCycleState.DESTROYED;
  }

  public void dumpComputationCachesToDisk() {
    for (String calcConfigurationName : getAllCalculationConfigurationNames()) {
      DependencyGraph depGraph = getDependencyGraph(calcConfigurationName);
      ViewComputationCache computationCache = getComputationCache(calcConfigurationName);

      TreeMap<String, Object> key2Value = new TreeMap<String, Object>();
      for (ValueSpecification outputSpec : depGraph.getOutputSpecifications()) {
        Object value = computationCache.getValue(outputSpec);
        key2Value.put(outputSpec.toString(), value);
      }

      try {
        File file = File.createTempFile("computation-cache-" + calcConfigurationName + "-", ".txt");
        s_logger.info("Dumping cache for calc conf " + calcConfigurationName + " to " + file.getAbsolutePath());
        FileWriter writer = new FileWriter(file);
        writer.write(key2Value.toString());
        writer.close();
      } catch (IOException e) {
        throw new RuntimeException("Writing cache to file failed", e);
      }
    }
  }

  //--------------------------------------------------------------------------
  public boolean isExecuted(DependencyNode node) {
    if (node == null) {
      return true;
    }
    _nodeExecutionLock.readLock().lock();
    try {
      return _executedNodes.contains(node);
    } finally {
      _nodeExecutionLock.readLock().unlock();
    }
  }

  public void markExecuted(DependencyNode node) {
    if (node == null) {
      return;
    }
    _nodeExecutionLock.writeLock().lock();
    try {
      _executedNodes.add(node);
    } finally {
      _nodeExecutionLock.writeLock().unlock();
    }
  }

  public boolean isFailed(DependencyNode node) {
    if (node == null) {
      return true;
    }
    _nodeExecutionLock.readLock().lock();
    try {
      return _failedNodes.contains(node);
    } finally {
      _nodeExecutionLock.readLock().unlock();
    }
  }

  public void markFailed(DependencyNode node) {
    if (node == null) {
      return;
    }
    _nodeExecutionLock.writeLock().lock();
    try {
      _failedNodes.add(node);
    } finally {
      _nodeExecutionLock.writeLock().unlock();
    }
  }

}
