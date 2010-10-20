/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.interpolation.LinearInterpolator1D;

/**
 * 
 */
@SuppressWarnings("unchecked")
public class SpreadCurveShiftFunctionTest {
  private static final int N = 10;
  private static final double[] X = new double[N];
  private static final double[] Y = new double[N];
  private static final InterpolatedDoublesCurve CURVE1;
  private static final LinearInterpolator1D LINEAR = new LinearInterpolator1D();
  private static final ConstantDoublesCurve CURVE2 = ConstantDoublesCurve.from(4, "B");
  private static final AddCurveSpreadFunction SPREAD_FUNCTION = new AddCurveSpreadFunction();
  private static final SpreadDoublesCurve SPREAD;
  private static final SpreadCurveShiftFunction F = new SpreadCurveShiftFunction();
  private static final double SHIFT = 0.12;
  private static final double EPS = 1e-15;

  static {
    for (int i = 0; i < N; i++) {
      X[i] = i;
      Y[i] = 2 * i + 1;
    }
    CURVE1 = InterpolatedDoublesCurve.fromSorted(X, Y, LINEAR, "A");
    SPREAD = SpreadDoublesCurve.from(new Curve[] {CURVE1, CURVE2}, SPREAD_FUNCTION);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurve1() {
    F.evaluate(null, 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurve2() {
    F.evaluate(null, 1, "B");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void test1() {
    F.evaluate(SPREAD, 2, 1);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void test2() {
    F.evaluate(SPREAD, 2, 1, "A");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void test3() {
    F.evaluate(SPREAD, new double[] {2}, new double[] {1});
  }

  @Test(expected = UnsupportedOperationException.class)
  public void test4() {
    F.evaluate(SPREAD, new double[] {2}, new double[] {1}, "A");
  }

  @Test
  public void testParallel() {
    SpreadDoublesCurve shifted = F.evaluate(SPREAD, SHIFT);
    Curve<Double, Double>[] c = shifted.getUnderlyingCurves();
    assertEquals(c[0], CURVE1);
    assertEquals(c[1], CURVE2);
    assertEquals(c[2].getClass(), ConstantDoublesCurve.class);
    assertArrayEquals(c[2].getYData(), new Double[] {SHIFT});
    for (int i = 0; i < N; i++) {
      assertEquals(SPREAD.getYValue(X[i]) + SHIFT, shifted.getYValue(X[i]), EPS);
    }
    assertEquals(shifted.getName(), "PARALLEL_SHIFT_" + SPREAD.getName());
    final String newName = "B";
    shifted = F.evaluate(SPREAD, SHIFT, newName);
    c = shifted.getUnderlyingCurves();
    assertEquals(c[0], CURVE1);
    assertEquals(c[1], CURVE2);
    assertEquals(c[2].getClass(), ConstantDoublesCurve.class);
    assertArrayEquals(c[2].getYData(), new Double[] {SHIFT});
    for (int i = 0; i < N; i++) {
      assertEquals(SPREAD.getYValue(X[i]) + SHIFT, shifted.getYValue(X[i]), EPS);
    }
    assertEquals(shifted.getName(), newName);
  }
}