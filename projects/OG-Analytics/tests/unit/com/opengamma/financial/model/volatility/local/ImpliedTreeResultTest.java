/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.local;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.opengamma.financial.model.tree.RecombiningBinomialTree;

/**
 * 
 */
public class ImpliedTreeResultTest {
  private static final Double[][] SPOT1 = new Double[][] {new Double[] {100.}, new Double[] {95., 105.}, new Double[] {90., 100., 110.}};
  private static final Double[][] VOL1 = new Double[][] {new Double[] {0.1}, new Double[] {0.05, 0.4}};
  private static final Double[][] SPOT2 = new Double[][] {new Double[] {200.}, new Double[] {95., 105.}, new Double[] {90., 100., 110.}};
  private static final Double[][] VOL2 = new Double[][] {new Double[] {0.2}, new Double[] {0.05, 0.4}};

  @Test(expected = IllegalArgumentException.class)
  public void testNullSpot() {
    new ImpliedTreeResult(null, new RecombiningBinomialTree<Double>(VOL1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullVol() {
    new ImpliedTreeResult(new RecombiningBinomialTree<Double>(SPOT1), null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWrongSize() {
    new ImpliedTreeResult(new RecombiningBinomialTree<Double>(SPOT1), new RecombiningBinomialTree<Double>(SPOT2));
  }

  @Test
  public void testGetters() {
    final ImpliedTreeResult result = new ImpliedTreeResult(new RecombiningBinomialTree<Double>(SPOT1), new RecombiningBinomialTree<Double>(VOL1));
    assertEquals(result.getSpotPriceTree(), new RecombiningBinomialTree<Double>(SPOT1));
    assertEquals(result.getLocalVolatilityTree(), new RecombiningBinomialTree<Double>(VOL1));
  }

  @Test
  public void testHashCodeAndEquals() {
    final ImpliedTreeResult result = new ImpliedTreeResult(new RecombiningBinomialTree<Double>(SPOT1), new RecombiningBinomialTree<Double>(VOL1));
    ImpliedTreeResult other = new ImpliedTreeResult(new RecombiningBinomialTree<Double>(SPOT1), new RecombiningBinomialTree<Double>(VOL1));
    assertEquals(result, other);
    assertEquals(result.hashCode(), other.hashCode());
    other = new ImpliedTreeResult(new RecombiningBinomialTree<Double>(SPOT2), new RecombiningBinomialTree<Double>(VOL1));
    assertFalse(result.equals(other));
    other = new ImpliedTreeResult(new RecombiningBinomialTree<Double>(SPOT1), new RecombiningBinomialTree<Double>(VOL2));
    assertFalse(result.equals(other));
  }
}