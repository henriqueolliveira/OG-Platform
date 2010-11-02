/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class MathMatrixTest extends AnalyticsTestBase {

  @Test
  public void test1D() {
    double[] data = new double[] { 1.0, 2.0 };
    DoubleMatrix1D matrix1 = new DoubleMatrix1D(data);
    DoubleMatrix1D matrix2 = cycleObject(DoubleMatrix1D.class, matrix1);
    assertEquals(matrix1, matrix2);
  }

  @Test
  public void test2D() {
    double[][] data = new double[][] { new double[] { 1.0, 2.0 }, new double[] { 3.0, 4.0 } };
    DoubleMatrix2D matrix1 = new DoubleMatrix2D(data);
    DoubleMatrix2D matrix2 = cycleObject(DoubleMatrix2D.class, matrix1);
    assertEquals(matrix1, matrix2);
  }

}