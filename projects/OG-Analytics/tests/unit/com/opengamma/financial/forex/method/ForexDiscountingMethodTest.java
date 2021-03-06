/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;
import java.util.Map;

import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.forex.definition.ForexDefinition;
import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.PresentValueSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Test related to the method for Forex transaction by discounting on each payment.
 */
public class ForexDiscountingMethodTest {

  private static final Currency CUR_1 = Currency.EUR;
  private static final Currency CUR_2 = Currency.USD;
  private static final ZonedDateTime PAYMENT_DATE = DateUtil.getUTCDate(2011, 5, 24);
  private static final double NOMINAL_1 = 100000000;
  private static final double FX_RATE = 1.4177;
  private static final ForexDefinition FX_DEFINITION = new ForexDefinition(CUR_1, CUR_2, PAYMENT_DATE, NOMINAL_1, FX_RATE);
  private static final YieldCurveBundle CURVES = ForexTestsDataSets.createCurvesForex();
  private static final String[] CURVES_NAME = CURVES.getAllNames().toArray(new String[0]);
  private static final ZonedDateTime REFERENCE_DATE = DateUtil.getUTCDate(2011, 5, 20);
  private static final Forex FX = FX_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAME);
  private static final PaymentFixedDefinition PAY_DEFINITION_1 = new PaymentFixedDefinition(CUR_1, PAYMENT_DATE, NOMINAL_1);
  private static final PaymentFixed PAY_1 = PAY_DEFINITION_1.toDerivative(REFERENCE_DATE, CURVES_NAME[0]);
  private static final PaymentFixedDefinition PAY_DEFINITION_2 = new PaymentFixedDefinition(CUR_2, PAYMENT_DATE, -NOMINAL_1 * FX_RATE);
  private static final PaymentFixed PAY_2 = PAY_DEFINITION_2.toDerivative(REFERENCE_DATE, CURVES_NAME[1]);

  private static final ForexDiscountingMethod METHOD = new ForexDiscountingMethod();
  private static final com.opengamma.financial.interestrate.PresentValueCalculator PVC_IR = com.opengamma.financial.interestrate.PresentValueCalculator.getInstance();
  private static final com.opengamma.financial.forex.calculator.PresentValueCalculator PVC_FX = com.opengamma.financial.forex.calculator.PresentValueCalculator.getInstance();
  private static final PresentValueSensitivityCalculator PVSC = PresentValueSensitivityCalculator.getInstance();

  @Test
  /**
   * Tests the present value computation.
   */
  public void presentValue() {
    MultipleCurrencyAmount pv = METHOD.presentValue(FX, CURVES);
    CurrencyAmount ca1 = CurrencyAmount.of(CUR_1, PVC_IR.visit(PAY_1, CURVES));
    CurrencyAmount ca2 = CurrencyAmount.of(CUR_2, PVC_IR.visit(PAY_2, CURVES));
    assertEquals(ca1, pv.getCurrencyAmountFor(CUR_1));
    assertEquals(ca2, pv.getCurrencyAmountFor(CUR_2));
  }

  @Test
  /**
   * Test the present value sensitivity to interest rate.
   */
  public void presentValueCurveSensitivity() {
    PresentValueSensitivity pvs = METHOD.presentValueCurveSensitivity(FX, CURVES);
    Map<String, List<DoublesPair>> pvs1 = PVSC.visit(PAY_1, CURVES);
    Map<String, List<DoublesPair>> pvs2 = PVSC.visit(PAY_2, CURVES);
    assertEquals(pvs1.get(CURVES_NAME[0]), pvs.getSensitivity().get(CURVES_NAME[0]));
    assertEquals(pvs2.get(CURVES_NAME[1]), pvs.getSensitivity().get(CURVES_NAME[1]));
  }

  @Test
  /**
   * Test the present value through the method and through the calculator.
   */
  public void presentValueMethodVsCalculator() {
    MultipleCurrencyAmount pvMethod = METHOD.presentValue(FX, CURVES);
    MultipleCurrencyAmount pvCalculator = PVC_FX.visit(FX, CURVES);
    assertEquals("Forex present value: Method vs Calculator", pvMethod, pvCalculator);
  }

  @Test
  /**
   * Test the present value of EUR/USD is the same as an USD/EUR.
   */
  public void presentValueReverse() {
    final ForexDefinition fxReverseDefinition = new ForexDefinition(CUR_2, CUR_1, PAYMENT_DATE, -NOMINAL_1 * FX_RATE, 1.0 / FX_RATE);
    final Forex fxReverse = fxReverseDefinition.toDerivative(REFERENCE_DATE, new String[] {CURVES_NAME[1], CURVES_NAME[0]});
    MultipleCurrencyAmount pv = METHOD.presentValue(FX, CURVES);
    MultipleCurrencyAmount pvReverse = METHOD.presentValue(fxReverse, CURVES);
    assertEquals("Forex present value: Reverse description", pv.getAmountFor(CUR_1), pvReverse.getAmountFor(CUR_1), 1.0E-2);
    assertEquals("Forex present value: Reverse description", pv.getAmountFor(CUR_2), pvReverse.getAmountFor(CUR_2), 1.0E-2);
  }

}
