/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.yield;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * 
 */
public class YieldConventionFactoryTest {
  private static final YieldConventionFactory FACTORY = YieldConventionFactory.INSTANCE;

  @Test(expected = IllegalArgumentException.class)
  public void testNullName() {
    FACTORY.getYieldConvention(null);
  }

  @Test
  public void testContainsAllInSimpleConvention() {
    assertEquals(FACTORY.getYieldConvention("UK STRIP METHOD"), SimpleYieldConvention.UK_STRIP_METHOD);
    assertEquals(FACTORY.getYieldConvention("UK:BUMP/DMO METHOD"), SimpleYieldConvention.UK_BUMP_DMO_METHOD);
    assertEquals(FACTORY.getYieldConvention("US I/L real"), SimpleYieldConvention.US_IL_REAL);
    assertEquals(FACTORY.getYieldConvention("US street"), SimpleYieldConvention.US_STREET);
    assertEquals(FACTORY.getYieldConvention("US Treasury equivalent"), SimpleYieldConvention.US_TREASURY_EQUIVALANT);
    assertEquals(FACTORY.getYieldConvention("Money Market"), SimpleYieldConvention.MONEY_MARKET);
    assertEquals(FACTORY.getYieldConvention("JGB simple"), SimpleYieldConvention.JGB_SIMPLE);
    assertEquals(FACTORY.getYieldConvention("True"), SimpleYieldConvention.TRUE);
    assertEquals(FACTORY.getYieldConvention("US Treasury"), SimpleYieldConvention.US_BOND);
  }
}