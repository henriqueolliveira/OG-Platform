/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.swap;

import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;

/**
 * Class to compute the quantities related to swaps (annuity, PVBP, coupon equivalent).
 */
public class SwapFixedIborMethod {

  /**
   * Present value calculator used for intermediary computations.
   */
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();

  /**
   * Computes the conventional cash annuity of a swap. The computation is relevant only for standard swaps with constant notional and regular payments.
   * @param fixedCouponSwap The underlying swap.
   * @param forward The swap forward.
   * @return The cash annuity.
   */
  public static double getAnnuityCash(FixedCouponSwap<? extends Payment> fixedCouponSwap, double forward) {
    int nbFixedPeriod = fixedCouponSwap.getFixedLeg().getPayments().length;
    int nbFixedPaymentYear = (int) Math.round(1.0 / fixedCouponSwap.getFixedLeg().getNthPayment(0).getPaymentYearFraction());
    double notional = Math.abs(fixedCouponSwap.getFixedLeg().getNthPayment(0).getNotional());
    double annuityCash = 1.0 / forward * (1.0 - 1.0 / Math.pow(1 + forward / nbFixedPaymentYear, nbFixedPeriod)) * notional;
    return annuityCash;
  }

  /**
   * Computes the physical annuity (also called PVBP or level) of the fixed leg of a swap.
   * @param fixedCouponSwap The underlying swap.
   * @param discountingCurve The discount curve.
   * @return The physical annuity.
   */
  public static double presentValueBasisPoint(FixedCouponSwap<? extends Payment> fixedCouponSwap, YieldAndDiscountCurve discountingCurve) {
    AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
    double pvbp = 0;
    for (int loopcpn = 0; loopcpn < annuityFixed.getPayments().length; loopcpn++) {
      pvbp += annuityFixed.getNthPayment(loopcpn).getPaymentYearFraction() * Math.abs(annuityFixed.getNthPayment(loopcpn).getNotional())
          * discountingCurve.getDiscountFactor(annuityFixed.getNthPayment(loopcpn).getPaymentTime());
    }
    return pvbp;
  }

  /**
   * Computes the physical annuity (also called PVBP or level) of the fixed leg of a swap.
   * @param fixedCouponSwap The underlying swap.
   * @param curves The yield curve bundle (containing the appropriate discounting curve).
   * @return The physical annuity.
   */
  public static double presentValueBasisPoint(FixedCouponSwap<? extends Payment> fixedCouponSwap, YieldCurveBundle curves) {
    AnnuityCouponFixed annuityFixed = fixedCouponSwap.getFixedLeg();
    YieldAndDiscountCurve discountingCurve = curves.getCurve(annuityFixed.getNthPayment(0).getFundingCurveName());
    double pvbp = 0;
    for (int loopcpn = 0; loopcpn < annuityFixed.getPayments().length; loopcpn++) {
      pvbp += annuityFixed.getNthPayment(loopcpn).getPaymentYearFraction() * Math.abs(annuityFixed.getNthPayment(loopcpn).getNotional())
          * discountingCurve.getDiscountFactor(annuityFixed.getNthPayment(loopcpn).getPaymentTime());
    }
    return pvbp;
  }

  /**
   * Computes the coupon equivalent of a swap (without margins).
   * @param fixedCouponSwap The underlying swap.
   * @param pvbp The swap PVBP.
   * @param curves The curves.
   * @return The coupon equivalent.
   */
  public static double couponEquivalent(FixedCouponSwap<? extends Payment> fixedCouponSwap, double pvbp, YieldCurveBundle curves) {
    return Math.abs(PVC.visit(fixedCouponSwap.getFixedLeg(), curves)) / pvbp;
  }

  /**
   * Computes the coupon equivalent of a swap (without margins).
   * @param fixedCouponSwap The underlying swap.
   * @param curves The curves.
   * @return The coupon equivalent.
   */
  public static double couponEquivalent(FixedCouponSwap<? extends Payment> fixedCouponSwap, YieldCurveBundle curves) {
    double pvbp = presentValueBasisPoint(fixedCouponSwap, curves);
    return couponEquivalent(fixedCouponSwap, pvbp, curves);
  }
}