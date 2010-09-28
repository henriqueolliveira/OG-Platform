/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic.twoasset;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.twoasset.StandardTwoAssetOptionDataBundle;
import com.opengamma.financial.model.option.definition.twoasset.TwoAssetCorrelationOptionDefinition;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class TwoAssetCorrelationOptionModelTest {
  private static final double S1 = 52;
  private static final double S2 = 65;
  private static final YieldAndDiscountCurve R = new ConstantYieldCurve(0.1);
  private static final double B1 = 0.1;
  private static final double B2 = 0.1;
  private static final VolatilitySurface SIGMA1 = new ConstantVolatilitySurface(0.2);
  private static final VolatilitySurface SIGMA2 = new ConstantVolatilitySurface(0.3);
  private static final double RHO = 0.75;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 7, 1);
  private static final StandardTwoAssetOptionDataBundle DATA = new StandardTwoAssetOptionDataBundle(R, B1, B2, SIGMA1, SIGMA2, S1, S2, RHO, DATE);
  private static final TwoAssetCorrelationOptionModel MODEL = new TwoAssetCorrelationOptionModel();
  private static final Expiry EXPIRY = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final TwoAssetCorrelationOptionDefinition OPTION = new TwoAssetCorrelationOptionDefinition(50, EXPIRY, true, 70);

  @Test(expected = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getPricingFunction(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getPricingFunction(OPTION).evaluate((StandardTwoAssetOptionDataBundle) null);
  }

  @Test
  public void test() {
    assertEquals(MODEL.getPricingFunction(OPTION).evaluate(DATA), 4.7073, 1e-4);
  }
}