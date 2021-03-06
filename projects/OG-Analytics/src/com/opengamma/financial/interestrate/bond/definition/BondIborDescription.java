/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Payment;

/**
 * Describes a Ibor floating coupon bond (Floating Rate Note) issue.
 */
public class BondIborDescription extends BondDescription<Payment> {

  /**
   * Ibor floating bond constructor from the nominal and the coupons.
   * @param nominal The notional payments. For bullet bond, it is restricted to a single payment.
   * @param coupon The bond Ibor coupons. The coupons notional should be in line with the bond nominal.
   */
  public BondIborDescription(AnnuityPaymentFixed nominal, GenericAnnuity<Payment> coupon) {
    super(nominal, coupon);
  }

  @Override
  public <S, T> T accept(InterestRateDerivativeVisitor<S, T> visitor, S data) {
    return null;
  }

  @Override
  public <T> T accept(InterestRateDerivativeVisitor<?, T> visitor) {
    return null;
  }

}
