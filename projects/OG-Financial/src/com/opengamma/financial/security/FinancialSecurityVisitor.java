/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.option.OptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * General visitor for top level asset classes.
 * 
 * @param <T> Return type for visitor.
 */
public interface FinancialSecurityVisitor<T> {

  T visitBondSecurity(BondSecurity security);

  T visitCashSecurity(CashSecurity security);

  T visitEquitySecurity(EquitySecurity security);

  T visitFRASecurity(FRASecurity security);

  T visitFutureSecurity(FutureSecurity security);

  T visitOptionSecurity(OptionSecurity security);

  T visitSwapSecurity(SwapSecurity security);

}
