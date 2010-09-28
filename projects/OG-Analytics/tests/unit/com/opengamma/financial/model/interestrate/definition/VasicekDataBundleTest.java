/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.volatility.curve.ConstantVolatilityCurve;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class VasicekDataBundleTest {
  private static final ConstantYieldCurve SHORT_RATE = new ConstantYieldCurve(0.02);
  private static final double LONG_RATE = 0.05;
  private static final double SPEED = 0.1;
  private static final ConstantVolatilityCurve SIGMA = new ConstantVolatilityCurve(0.4);
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 1, 1);
  private static final VasicekDataBundle DATA = new VasicekDataBundle(SHORT_RATE, SIGMA, DATE, LONG_RATE, SPEED);

  @Test(expected = IllegalArgumentException.class)
  public void testNullShortRate() {
    new VasicekDataBundle(null, SIGMA, DATE, LONG_RATE, SPEED);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullVolatility() {
    new VasicekDataBundle(SHORT_RATE, null, DATE, LONG_RATE, SPEED);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDate() {
    new VasicekDataBundle(SHORT_RATE, SIGMA, null, LONG_RATE, SPEED);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testZeroSpeed() {
    new VasicekDataBundle(SHORT_RATE, SIGMA, DATE, LONG_RATE, 0);
  }

  @Test
  public void testGetters() {
    final double t = Math.random();
    assertEquals(DATA.getShortRateCurve(), SHORT_RATE);
    assertEquals(DATA.getShortRate(t), SHORT_RATE.getInterestRate(t), 0);
    assertEquals(DATA.getLongTermInterestRate(), LONG_RATE, 0);
    assertEquals(DATA.getReversionSpeed(), SPEED, 0);
    assertEquals(DATA.getShortRateVolatilityCurve(), SIGMA);
    assertEquals(DATA.getShortRateVolatility(t), SIGMA.getVolatility(t), 0);
    assertEquals(DATA.getDate(), DATE);
  }

  @Test
  public void testHashCodeAndEquals() {
    VasicekDataBundle other = new VasicekDataBundle(SHORT_RATE, SIGMA, DATE, LONG_RATE, SPEED);
    assertEquals(other, DATA);
    assertEquals(other.hashCode(), DATA.hashCode());
    other = new VasicekDataBundle(new ConstantYieldCurve(SHORT_RATE.getInterestRate(0.) + 1), SIGMA, DATE, LONG_RATE, SPEED);
    assertFalse(other.equals(DATA));
    other = new VasicekDataBundle(SHORT_RATE, new ConstantVolatilityCurve(SIGMA.getVolatility(0.) + 0.2), DATE, LONG_RATE, SPEED);
    assertFalse(other.equals(DATA));
    other = new VasicekDataBundle(SHORT_RATE, SIGMA, DATE.plusDays(10), LONG_RATE, SPEED);
    assertFalse(other.equals(DATA));
    other = new VasicekDataBundle(SHORT_RATE, SIGMA, DATE, LONG_RATE + 1, SPEED);
    assertFalse(other.equals(DATA));
    other = new VasicekDataBundle(SHORT_RATE, SIGMA, DATE, LONG_RATE, SPEED + 1);
    assertFalse(other.equals(DATA));
  }
}