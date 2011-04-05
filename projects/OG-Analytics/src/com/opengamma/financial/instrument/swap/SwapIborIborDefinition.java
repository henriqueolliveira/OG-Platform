/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swap;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.financial.instrument.payment.CouponIborSpreadDefinition;

/**
 * 
 */
public class SwapIborIborDefinition extends SwapDefinition<CouponIborSpreadDefinition, CouponIborSpreadDefinition> {
  /**
   * Constructor of the ibor-ibor swap from its two legs. The first leg has no spread, the second leg has a spread.
   * @param firstLeg The first Ibor leg.
   * @param secondLeg The second Ibor leg.
   */
  public SwapIborIborDefinition(AnnuityCouponIborSpreadDefinition firstLeg, AnnuityCouponIborSpreadDefinition secondLeg) {
    super(firstLeg, secondLeg);
    Validate.isTrue(firstLeg.getCurrency() == secondLeg.getCurrency(), "legs should have the same currency");
  }

  /**
   * The leg with no spread.
   * @return The annuity.
   */
  public AnnuityCouponIborSpreadDefinition getLegWithoutSpread() {
    return (AnnuityCouponIborSpreadDefinition) getFirstLeg();
  }

  /**
   * The leg with the spread.
   * @return The annuity.
   */
  public AnnuityCouponIborSpreadDefinition getLegWithSpread() {
    return (AnnuityCouponIborSpreadDefinition) getSecondLeg();
  }

  @Override
  public <U, V> V accept(FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitSwapIborIborDefinition(this, data);
  }

  @Override
  public <V> V accept(FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitSwapIborIborDefinition(this);
  }

}