/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

import org.junit.Test;

import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;

/**
 * 
 */
public class SABRConjugateGradientLeastSquareFitterTest extends LeastSquareSmileFitterTestCase {
  private static final SABRConjugateGradientLeastSquareFitter FITTER = new SABRConjugateGradientLeastSquareFitter(new SABRHaganVolatilityFunction());
  private static final double[] INITIAL_VALUES = new double[] {0.5, 1, 0.2, 0};

  @Test(expected = IllegalArgumentException.class)
  public void testNullSABRFormula() {
    new SABRConjugateGradientLeastSquareFitter(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testNullErrors() {
    FITTER.getFitResult(null, null, null, null);
  }

  @Override
  protected LeastSquareSmileFitter getFitter() {
    return FITTER;
  }

  @Override
  protected double[] getInitialValues() {
    return INITIAL_VALUES;
  }

}