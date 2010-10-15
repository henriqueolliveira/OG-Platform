/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.time.Instant;
import javax.time.InstantProvider;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.tuple.Pair;

/**
 * Implements a {@link FunctionRepositoryCompiler} that caches the results of previous compilations so
 * that a minimal compilation is performed each time. 
 */
public class CachingFunctionRepositoryCompiler implements FunctionRepositoryCompiler {

  private final TreeMap<Pair<FunctionRepository, Instant>, InMemoryCompiledFunctionRepository> _compilationCache = new TreeMap<Pair<FunctionRepository, Instant>, InMemoryCompiledFunctionRepository>();
  private final Queue<Pair<FunctionRepository, Instant>> _activeEntries = new ArrayDeque<Pair<FunctionRepository, Instant>>();
  private int _cacheSize = 16;

  public synchronized void setCacheSize(final int cacheSize) {
    _cacheSize = cacheSize;
    while (_activeEntries.size() > cacheSize) {
      _compilationCache.remove(_activeEntries.remove());
    }
  }

  public int getCacheSize() {
    return _cacheSize;
  }

  protected TreeMap<Pair<FunctionRepository, Instant>, InMemoryCompiledFunctionRepository> getCompilationCache() {
    return _compilationCache;
  }

  protected Queue<Pair<FunctionRepository, Instant>> getActiveCacheEntries() {
    return _activeEntries;
  }

  protected boolean addFunctionFromCachedRepository(final InMemoryCompiledFunctionRepository before, final InMemoryCompiledFunctionRepository after, final InMemoryCompiledFunctionRepository compiled,
      final FunctionDefinition function, final Instant atInstant) {
    if (before != null) {
      final CompiledFunctionDefinition compiledFunction = before.findDefinition(function.getUniqueIdentifier());
      if (compiledFunction.getLatestInvocationTime() == null) {
        // previous one always valid
        compiled.addFunction(compiledFunction);
        return true;
      } else {
        final Instant validUntil = Instant.of(compiledFunction.getLatestInvocationTime());
        if (!validUntil.isBefore(atInstant)) {
          // previous one still valid
          compiled.addFunction(compiledFunction);
          return true;
        }
      }
    }
    if (after != null) {
      final CompiledFunctionDefinition compiledFunction = after.findDefinition(function.getUniqueIdentifier());
      if (compiledFunction.getEarliestInvocationTime() == null) {
        // next one always valid
        compiled.addFunction(compiledFunction);
        return true;
      } else {
        final Instant validFrom = Instant.of(compiledFunction.getEarliestInvocationTime());
        if (!validFrom.isAfter(atInstant)) {
          // next one already valid
          compiled.addFunction(compiledFunction);
          return true;
        }
      }
    }
    return false;
  }

  protected InMemoryCompiledFunctionRepository compile(final FunctionCompilationContext context, final FunctionRepository functions, final Instant atInstant,
      final InMemoryCompiledFunctionRepository before, final InMemoryCompiledFunctionRepository after, final ExecutorService executorService) {
    final InMemoryCompiledFunctionRepository compiled = new InMemoryCompiledFunctionRepository(context);
    final ExecutorCompletionService<CompiledFunctionDefinition> completionService = new ExecutorCompletionService<CompiledFunctionDefinition>(executorService);
    int numCompiles = 0;
    for (final FunctionDefinition function : functions.getAllFunctions()) {
      if (addFunctionFromCachedRepository(before, after, compiled, function, atInstant)) {
        continue;
      }
      completionService.submit(new Callable<CompiledFunctionDefinition>() {
        @Override
        public CompiledFunctionDefinition call() throws Exception {
          return function.compile(context, atInstant);
        }
      });
      numCompiles++;
    }
    for (int i = 0; i < numCompiles; i++) {
      Future<CompiledFunctionDefinition> future;
      try {
        future = completionService.take();
      } catch (InterruptedException e1) {
        Thread.interrupted();
        throw new OpenGammaRuntimeException("Interrupted while compiling function definitions.");
      }
      try {
        CompiledFunctionDefinition compiledFunction = future.get();
        compiled.addFunction(compiledFunction);
      } catch (Exception e) {
        throw new OpenGammaRuntimeException("Exception thrown compiling function definitions.", e);
      }
    }
    return compiled;
  }

  protected synchronized InMemoryCompiledFunctionRepository getCachedCompilation(final Pair<FunctionRepository, Instant> key) {
    return getCompilationCache().get(key);
  }

  protected synchronized InMemoryCompiledFunctionRepository getPreviousCompilation(final Pair<FunctionRepository, Instant> key) {
    final Map.Entry<Pair<FunctionRepository, Instant>, InMemoryCompiledFunctionRepository> entry = getCompilationCache().lowerEntry(key);
    if ((entry != null) && (entry.getKey().getFirst() == key.getFirst())) {
      return entry.getValue();
    }
    return null;
  }

  protected synchronized InMemoryCompiledFunctionRepository getNextCompilation(final Pair<FunctionRepository, Instant> key) {
    final Map.Entry<Pair<FunctionRepository, Instant>, InMemoryCompiledFunctionRepository> entry = getCompilationCache().higherEntry(key);
    if ((entry != null) && (entry.getKey().getFirst() == key.getFirst())) {
      return entry.getValue();
    }
    return null;
  }

  protected synchronized void cacheCompilation(final Pair<FunctionRepository, Instant> key, final InMemoryCompiledFunctionRepository compiled) {
    final Queue<Pair<FunctionRepository, Instant>> active = getActiveCacheEntries();
    if (active.size() >= getCacheSize()) {
      getCompilationCache().remove(active.remove());
    }
    getCompilationCache().put(key, compiled);
    getActiveCacheEntries().add(key);
  }

  @Override
  public CompiledFunctionRepository compile(final CompiledFunctionService context, final InstantProvider atInstantProvider) {
    final Instant atInstant = Instant.of(atInstantProvider);
    final Pair<FunctionRepository, Instant> key = Pair.of(context.getFunctionRepository(), atInstant);
    // Try a previous compilation
    final InMemoryCompiledFunctionRepository previous = getPreviousCompilation(key);
    if (previous != null) {
      if (previous.getLatestInvocationTime() == null) {
        return previous;
      } else {
        if (!atInstant.isAfter(previous.getLatestInvocationTime())) {
          return previous;
        }
      }
    }
    // Try a future compilation
    final InMemoryCompiledFunctionRepository next = getNextCompilation(key);
    if (next != null) {
      if (next.getEarliestInvocationTime() == null) {
        return next;
      } else {
        if (!atInstant.isBefore(next.getEarliestInvocationTime())) {
          return next;
        }
      }
    }
    // Try the exact timestamp
    InMemoryCompiledFunctionRepository compiled = getCachedCompilation(key);
    if (compiled != null) {
      return compiled;
    }
    // Create a compilation, salvaging results from previous and next if possible
    compiled = compile(context.getFunctionCompilationContext(), context.getFunctionRepository(), atInstant, previous, next, context.getExecutorService());
    cacheCompilation(key, compiled);
    return compiled;
  }

}