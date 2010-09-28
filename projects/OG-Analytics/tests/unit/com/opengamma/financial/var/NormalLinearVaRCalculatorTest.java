/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;

/**
 * 
 */
public class NormalLinearVaRCalculatorTest {
  private static final double HORIZON = 10;
  private static final double PERIODS = 250;
  private static final double QUANTILE = new NormalDistribution(0, 1).getCDF(3.);
  private static final Function1D<NormalStatistics<?>, Double> CALCULATOR = new NormalLinearVaRCalculator(HORIZON, PERIODS, QUANTILE);

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    CALCULATOR.evaluate((NormalStatistics<?>) null);
  }

  @Test
  public void test() {
    final NormalStatistics<Double> stats = new NormalStatistics<Double>(new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return 0.4;
      }
    }, new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return 1.;
      }

    }, 0.);
    assertEquals(CALCULATOR.evaluate(stats), 3 * 0.2 - 0.016, 1e-9);
  }

  @Test
  public void testEqualsAndHashCode() {
    NormalLinearVaRCalculator calculator = new NormalLinearVaRCalculator(HORIZON, PERIODS, QUANTILE);
    assertEquals(calculator, CALCULATOR);
    assertEquals(calculator.hashCode(), CALCULATOR.hashCode());
    calculator.setHorizon(HORIZON - 1);
    assertFalse(calculator.equals(CALCULATOR));
    calculator.setHorizon(HORIZON);
    calculator.setPeriods(PERIODS - 1);
    assertFalse(calculator.equals(CALCULATOR));
    calculator.setPeriods(PERIODS);
    calculator.setQuantile(0.95);
    assertFalse(calculator.equals(CALCULATOR));
  }
}