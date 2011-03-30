/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.execution;

import javax.time.Instant;

import org.fudgemsg.util.ArgumentChecker;

/**
 * Encapsulates specific settings affecting the execution of an individual view cycle.
 */
public class ViewCycleExecutionOptions {

  private final Instant _valuationTime;
  private final Instant _inputDataTime;
  
  // TODO [PLAT-1153] Correction time
  
  public ViewCycleExecutionOptions(Instant valuationTime, Instant inputDataTime) {
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    ArgumentChecker.notNull(inputDataTime, "inputDataTime");
    
    _valuationTime = valuationTime;
    _inputDataTime = inputDataTime;
  }

  /**
   * Gets the valuation time for use by the analytics library.
   * 
   * @return the valuation time, not null
   */
  public Instant getValuationTime() {
    return _valuationTime;
  }

  /**
   * Gets the time used to determine the set of input data to use for the computation cycle.
   * 
   * @return the input data time, not null
   */
  public Instant getInputDataTime() {
    return _inputDataTime;
  }

  @Override
  public String toString() {
    return "ViewExecutionOptions[valuationTime=" + _valuationTime + ", inputDataTime=" + _inputDataTime + "]";
  }
  
}