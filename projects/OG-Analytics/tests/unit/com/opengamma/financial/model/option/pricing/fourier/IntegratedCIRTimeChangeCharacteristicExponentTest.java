/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * 
 */
public class IntegratedCIRTimeChangeCharacteristicExponentTest {
  private static final double KAPPA = 0.5;
  private static final double THETA = 0.8;
  private static final double LAMBDA = 1;
  private static final IntegratedCIRTimeChangeCharacteristicExponent1 EXPONENT = new IntegratedCIRTimeChangeCharacteristicExponent1(KAPPA, THETA, LAMBDA);

  @Test
  public void test() {
    assertEquals(EXPONENT.getKappa(), KAPPA, 0);
    assertEquals(EXPONENT.getLambda(), LAMBDA, 0);
    assertEquals(EXPONENT.getTheta(), THETA, 0);
    IntegratedCIRTimeChangeCharacteristicExponent1 other = new IntegratedCIRTimeChangeCharacteristicExponent1(KAPPA, THETA, LAMBDA);
    assertEquals(other, EXPONENT);
    assertEquals(other.hashCode(), EXPONENT.hashCode());
    other = new IntegratedCIRTimeChangeCharacteristicExponent1(KAPPA + 1, THETA, LAMBDA);
    assertFalse(other.equals(EXPONENT));
    other = new IntegratedCIRTimeChangeCharacteristicExponent1(KAPPA, THETA + 1, LAMBDA);
    assertFalse(other.equals(EXPONENT));
    other = new IntegratedCIRTimeChangeCharacteristicExponent1(KAPPA, THETA, LAMBDA + 1);
    assertFalse(other.equals(EXPONENT));
  }

  @Test
  public void testAlphas() {
    assertEquals(EXPONENT.getSmallestAlpha(), Double.NEGATIVE_INFINITY, 0);
  }
}