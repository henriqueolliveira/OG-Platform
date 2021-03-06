/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.definition;

import static org.testng.AssertJUnit.assertArrayEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;

/**
 * 
 */
public class BondFutureDeliverableBasketDataBundleTest {
  private static final double[] CLEAN_PRICES = new double[] {101., 102., 103., 104.};
  private static final double[] REPO_RATES = new double[] {0.03, 0.04, 0.04, 0.01};

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCleanPrices() {
    new BondFutureDeliverableBasketDataBundle(null, REPO_RATES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRepoRates() {
    new BondFutureDeliverableBasketDataBundle(CLEAN_PRICES, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testArrays() {
    new BondFutureDeliverableBasketDataBundle(new double[] {100., 103.}, REPO_RATES);
  }

  @Test
  public void testGetters() {
    final BondFutureDeliverableBasketDataBundle basketData = new BondFutureDeliverableBasketDataBundle(CLEAN_PRICES, REPO_RATES);
    assertArrayEquals(basketData.getCleanPrices(), CLEAN_PRICES, 0);
    assertArrayEquals(basketData.getRepoRates(), REPO_RATES, 0);
    assertEquals(basketData.getBasketSize(), 4);
  }

  @Test
  public void testHashCodeAndEquals() {
    final BondFutureDeliverableBasketDataBundle basketData = new BondFutureDeliverableBasketDataBundle(CLEAN_PRICES, REPO_RATES);
    BondFutureDeliverableBasketDataBundle other = new BondFutureDeliverableBasketDataBundle(CLEAN_PRICES, REPO_RATES);
    assertEquals(basketData, other);
    assertEquals(basketData.hashCode(), other.hashCode());
    other = new BondFutureDeliverableBasketDataBundle(CLEAN_PRICES, CLEAN_PRICES);
    assertFalse(other.equals(basketData));
    other = new BondFutureDeliverableBasketDataBundle(REPO_RATES, REPO_RATES);
    assertFalse(other.equals(basketData));
  }
}
