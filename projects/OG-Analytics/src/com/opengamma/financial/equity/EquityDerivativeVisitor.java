/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity;

import com.opengamma.financial.equity.future.EquityIndexDividendFuture; 
// !!! (Why) must I import the above?

/**
 * 
 * @param <S> The type of the data
 * @param <T> The return type of the calculation
 */
public interface EquityDerivativeVisitor<S, T> {
  
  T visit(EquityDerivative derivative, S data);
  T visit(EquityDerivative derivative);

  T visitEquityIndexDividendFuture(EquityIndexDividendFuture equityIndexDividendFuture, S data);
  T visitEquityIndexDividendFuture(EquityIndexDividendFuture equityIndexDividendFuture);
  
  /*
   * TODO:  
   *  a) Include here the initial list of equity derivatives that we wish to handle
   *  b) Build the functionality for them
   */

  // T visitEquitySingleStockDividendFuture(EquitySingleStockDividendFuture equitySingleStockDividendFuture, S data);
  
  // T visitEquityVarianceSwap(EquityVarianceSwap equityVarianceSwap, S data);

}
