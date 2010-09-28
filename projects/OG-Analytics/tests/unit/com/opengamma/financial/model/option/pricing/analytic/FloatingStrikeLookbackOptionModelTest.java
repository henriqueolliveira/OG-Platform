/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.FloatingStrikeLookbackOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.definition.StandardOptionWithSpotTimeSeriesDataBundle;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;

/**
 * 
 */
public class FloatingStrikeLookbackOptionModelTest {
  private static final YieldAndDiscountCurve CURVE = new ConstantYieldCurve(0.1);
  private static final double B = 0.04;
  private static final VolatilitySurface SURFACE = new ConstantVolatilitySurface(0.3);
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 7, 1);
  private static final double SPOT = 120;
  private static final DoubleTimeSeries<?> TS = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new long[] {1, 2, 3, 4, 5, 6, 7}, new double[] {100, 101, 106, 100, 109,
      101, 104});
  private static final StandardOptionWithSpotTimeSeriesDataBundle DATA = new StandardOptionWithSpotTimeSeriesDataBundle(CURVE, B, SURFACE, SPOT, DATE, TS);
  private static final Expiry EXPIRY = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final FloatingStrikeLookbackOptionDefinition CALL = new FloatingStrikeLookbackOptionDefinition(EXPIRY, true);
  private static final FloatingStrikeLookbackOptionDefinition PUT = new FloatingStrikeLookbackOptionDefinition(EXPIRY, false);
  private static final AnalyticOptionModel<FloatingStrikeLookbackOptionDefinition, StandardOptionWithSpotTimeSeriesDataBundle> MODEL = new FloatingStrikeLookbackOptionModel();
  private static final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> BSM = new BlackScholesMertonModel();

  @Test(expected = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getPricingFunction(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getPricingFunction(CALL).evaluate((StandardOptionWithSpotTimeSeriesDataBundle) null);
  }

  @Test
  public void test() {
    double strike = 102;
    DoubleTimeSeries<?> shortTS = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new long[] {1}, new double[] {strike});
    StandardOptionWithSpotTimeSeriesDataBundle data = DATA.withSpotTimeSeries(shortTS).withVolatilitySurface(new ConstantVolatilitySurface(0));
    OptionDefinition vanilla = new EuropeanVanillaOptionDefinition(strike, EXPIRY, true);
    assertEquals(MODEL.getPricingFunction(CALL).evaluate(data), BSM.getPricingFunction(vanilla).evaluate(data), 1e-9);
    data = data.withCostOfCarry(0);
    assertEquals(MODEL.getPricingFunction(CALL).evaluate(data), BSM.getPricingFunction(vanilla).evaluate(data), 1e-9);
    strike = 95;
    shortTS = new FastArrayLongDoubleTimeSeries(DateTimeNumericEncoding.DATE_EPOCH_DAYS, new long[] {1}, new double[] {strike});
    data = DATA.withSpotTimeSeries(shortTS).withVolatilitySurface(new ConstantVolatilitySurface(0));
    vanilla = new EuropeanVanillaOptionDefinition(strike, EXPIRY, false);
    assertEquals(MODEL.getPricingFunction(PUT).evaluate(data), BSM.getPricingFunction(vanilla).evaluate(data), 1e-9);
    data = data.withCostOfCarry(0);
    assertEquals(MODEL.getPricingFunction(PUT).evaluate(data), BSM.getPricingFunction(vanilla).evaluate(data), 1e-9);
    assertEquals(MODEL.getPricingFunction(CALL).evaluate(DATA), 25.3533, 1e-4);
  }
}