/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.function;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import com.opengamma.math.differentiation.ScalarFieldFirstOrderDifferentiator;
import com.opengamma.math.differentiation.ScalarFirstOrderDifferentiator;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ParameterizedFunctionTest {

  private static ParameterizedFunction<Double, double[], Double> ARRAY_PARAMS = new ParameterizedFunction<Double, double[], Double>() {

    @Override
    public Double evaluate(Double x, double[] a) {
      int n = a.length;
      double sum = 0.0;
      for (int i = n - 1; i > 0; i--) {
        sum += a[i];
        sum *= x;
      }
      sum += a[0];
      return sum;
    }
  };

  private static ParameterizedFunction<Double, DoubleMatrix1D, Double> VECTOR_PARAMS = new ParameterizedFunction<Double, DoubleMatrix1D, Double>() {

    @Override
    public Double evaluate(Double x, DoubleMatrix1D a) {
      ArgumentChecker.notNull(a, "parameters");
      if (a.getNumberOfElements() != 2) {
        throw new IllegalArgumentException("wrong number of parameters");
      }
      return a.getEntry(0) * Math.sin(a.getEntry(1) * x);
    }
  };

  @Test
  public void TestCubic() {
    double[] parms = new double[] {3.0, -1.0, 1.0, 1.0};
    assertEquals(13.0, ARRAY_PARAMS.evaluate(2.0, parms), 0.0);

    Function1D<Double, Double> func = ARRAY_PARAMS.asFunctionOfArguments(parms);
    assertEquals(4.0, func.evaluate(-1.0), 0.0);

    Function1D<double[], Double> param_func = ARRAY_PARAMS.asFunctionOfParameters(0.0);
    assertEquals(10.0, param_func.evaluate(new double[] {10, 312, 423, 534}), 0.0);
  }

  @Test
  public void TestSin() {
    DoubleMatrix1D parms = new DoubleMatrix1D(new double[] {-1.0, 0.5});
    assertEquals(-Math.sin(1.0), VECTOR_PARAMS.evaluate(2.0, parms), 0.0);

    Function1D<Double, Double> func = VECTOR_PARAMS.asFunctionOfArguments(parms);
    assertEquals(1.0, func.evaluate(-Math.PI), 0.0);

    ScalarFirstOrderDifferentiator diff = new ScalarFirstOrderDifferentiator();
    Function1D<Double, Double> grad = diff.differentiate(func);
    assertEquals(-0.5, grad.evaluate(0.0), 1e-8);

    Function1D<DoubleMatrix1D, Double> params_func = VECTOR_PARAMS.asFunctionOfParameters(1.0);
    ScalarFieldFirstOrderDifferentiator vdiff = new ScalarFieldFirstOrderDifferentiator();
    Function1D<DoubleMatrix1D, DoubleMatrix1D> vgrad = vdiff.differentiate(params_func);
    DoubleMatrix1D res = vgrad.evaluate(new DoubleMatrix1D(new double[] {Math.PI, 0}));
    assertEquals(0.0, res.getEntry(0), 1e-8);
    assertEquals(Math.PI, res.getEntry(1), 1e-8);
  }
}
