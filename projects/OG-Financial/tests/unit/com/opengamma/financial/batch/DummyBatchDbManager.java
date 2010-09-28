/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.OffsetTime;

import com.opengamma.engine.view.calc.DependencyGraphExecutorFactory;
import com.opengamma.engine.view.calc.SingleNodeExecutorFactory;

/**
 * 
 */
public class DummyBatchDbManager implements BatchDbManager {
  
  private Map<SnapshotId, Set<LiveDataValue>> _snapshot2LiveData = new HashMap<SnapshotId, Set<LiveDataValue>>();
  
  @Override
  public void startBatch(BatchJobRun batch) {
  }

  @Override
  public void endBatch(BatchJobRun batch) {
  }

  @Override
  public void createLiveDataSnapshot(SnapshotId snapshotId) {
  }

  @Override
  public void fixLiveDataSnapshotTime(SnapshotId snapshotId, OffsetTime fix) {
  }

  @Override
  public void markLiveDataSnapshotComplete(SnapshotId snapshotId) {
  }

  @Override
  public void addValuesToSnapshot(SnapshotId snapshotId, Set<LiveDataValue> values) {
  }

  @Override
  public Set<LiveDataValue> getSnapshotValues(SnapshotId snapshotId) {
    Set<LiveDataValue> returnValue = _snapshot2LiveData.get(snapshotId);
    if (returnValue == null) {
      throw new IllegalArgumentException(snapshotId.toString());
    }
    return returnValue;
  }

  @Override
  public DependencyGraphExecutorFactory<?> createDependencyGraphExecutorFactory(BatchJobRun batch) {
    return new SingleNodeExecutorFactory();
  }
  
  public void addLiveData(SnapshotId snapshot, LiveDataValue value) {
    Set<LiveDataValue> values = _snapshot2LiveData.get(snapshot);
    if (values == null) {
      values = new HashSet<LiveDataValue>();
      _snapshot2LiveData.put(snapshot, values);
    }
    values.add(value);
  }

}