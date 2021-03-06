/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.util.PublicAPI;

/**
 * The result of a single configuration of a view calculation. 
 */
@PublicAPI
public interface ViewCalculationResultModel {
  
  /**
   * Returns all terminal output target specifications present in the configuration.
   * 
   * @return the target specifications
   */
  Collection<ComputationTargetSpecification> getAllTargets();

  /**
   * Returns the computed values for a given target. The values are returned as a map of value names to {@link ComputedValue}s.
   * If multiple values were produced for a given value name, an arbitrary choice is made for which to include in the map.  
   * 
   * @param target the target to search for, not {@code null}
   * @return the computed values for this configuration, or {@code null} if the target does not exist in the view results
   */
  Map<String, ComputedValue> getValues(ComputationTargetSpecification target);
  
  /**
   * Returns all computed values for a given target.
   * 
   * @param target the target to search for, not {@code null}
   * @return the computed values for this configuration, or {@code null} if the target does not exist in the view results
   */
  Set<ComputedValue> getAllValues(ComputationTargetSpecification target);

}
