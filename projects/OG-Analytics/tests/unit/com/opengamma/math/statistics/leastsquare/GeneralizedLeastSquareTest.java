/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.statistics.leastsquare;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.BasisFunctionAggregation;
import com.opengamma.math.interpolation.BasisFunctionGenerator;
import com.opengamma.math.interpolation.PSplineFitter;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;

/**
 * 
 */
public class GeneralizedLeastSquareTest {
  private static boolean PRINT = false;

  protected static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  private static final NormalDistribution NORMAL = new NormalDistribution(0, 1.0, RANDOM);
  private static final double[] WEIGHTS = new double[] {1.0, -0.5, 2.0, 0.23, 1.45};
  // private static final double[] WEIGHTS = new double[] {1.0, 1.0, 0.0, 0.0, 0};
  private static final Double[] X;
  private static final double[] Y;
  private static final double[] SIGMA;
  private static final List<DoubleMatrix1D> X_TRIG;
  private static final List<Double> Y_TRIG;
  private static final List<Double> SIGMA_TRIG;
  private static final List<Double> SIGMA_COS_EXP;
  private static final List<double[]> X_COS_EXP;
  private static final List<Double> Y_COS_EXP;
  private static final List<Function1D<Double, Double>> SIN_FUNCTIONS;
  private static final Function1D<Double, Double> TEST_FUNCTION;
  private static final List<Function1D<Double, Double>> BASIS_FUNCTIONS;
  private static final List<Function1D<double[], Double>> BASIS_FUNCTIONS_2D;
  private static Function1D<double[], Double> COS_EXP_FUNCTION;

  private static final List<Function1D<DoubleMatrix1D, Double>> VECTOR_TRIG_FUNCTIONS;
  private static final Function1D<DoubleMatrix1D, Double> VECTOR_TEST_FUNCTION;

  static {
    SIN_FUNCTIONS = new ArrayList<Function1D<Double, Double>>();
    for (int i = 0; i < WEIGHTS.length; i++) {
      final int k = i;
      Function1D<Double, Double> func = new Function1D<Double, Double>() {

        @Override
        public Double evaluate(Double x) {
          return Math.sin((2 * k + 1) * x);
        }
      };
      SIN_FUNCTIONS.add(func);
    }
    TEST_FUNCTION = new BasisFunctionAggregation<Double>(SIN_FUNCTIONS, WEIGHTS);

    VECTOR_TRIG_FUNCTIONS = new ArrayList<Function1D<DoubleMatrix1D, Double>>();
    for (int i = 0; i < WEIGHTS.length; i++) {
      final int k = i;
      Function1D<DoubleMatrix1D, Double> func = new Function1D<DoubleMatrix1D, Double>() {
        @Override
        public Double evaluate(DoubleMatrix1D x) {
          Validate.isTrue(x.getNumberOfElements() == 2);
          return Math.sin((2 * k + 1) * x.getEntry(0)) * Math.cos((2 * k + 1) * x.getEntry(1));
        }
      };
      VECTOR_TRIG_FUNCTIONS.add(func);
    }
    VECTOR_TEST_FUNCTION = new BasisFunctionAggregation<DoubleMatrix1D>(VECTOR_TRIG_FUNCTIONS, WEIGHTS);

    COS_EXP_FUNCTION = new Function1D<double[], Double>() {

      @Override
      public Double evaluate(double[] x) {
        return Math.sin(Math.PI * x[0] / 10.0) * Math.exp(-x[1] / 5.);
      }
    };

    int n = 10;

    X = new Double[n];
    Y = new double[n];
    SIGMA = new double[n];
    X_TRIG = new ArrayList<DoubleMatrix1D>();
    Y_TRIG = new ArrayList<Double>();
    SIGMA_TRIG = new ArrayList<Double>();
    for (int i = 0; i < n; i++) {
      X[i] = i / 5.0;
      Y[i] = TEST_FUNCTION.evaluate(X[i]);
      double[] temp = new double[2];
      temp[0] = 2.0 * RANDOM.nextDouble();
      temp[1] = 2.0 * RANDOM.nextDouble();
      X_TRIG.add(new DoubleMatrix1D(temp));
      Y_TRIG.add(VECTOR_TEST_FUNCTION.evaluate(X_TRIG.get(i)));
      SIGMA[i] = 0.01;
      SIGMA_TRIG.add(0.01);
    }

    SIGMA_COS_EXP = new ArrayList<Double>();
    X_COS_EXP = new ArrayList<double[]>();
    Y_COS_EXP = new ArrayList<Double>();
    for (int i = 0; i < 20; i++) {
      double[] temp = new double[2];
      temp[0] = 10.0 * RANDOM.nextDouble();
      temp[1] = 10.0 * RANDOM.nextDouble();
      X_COS_EXP.add(temp);
      Y_COS_EXP.add(COS_EXP_FUNCTION.evaluate(X_COS_EXP.get(i)));
      SIGMA_COS_EXP.add(0.01);
    }

    BasisFunctionGenerator generator = new BasisFunctionGenerator();
    BASIS_FUNCTIONS = generator.generateSet(0.0, 2.0, 20, 3);
    BASIS_FUNCTIONS_2D = generator.generateSet(new double[] {0.0, 0.0}, new double[] {10.0, 10.0}, new int[] {10, 10}, new int[] {3, 3});

    // for (int i = 0; i < 101; i++) {
    // double xx = 0 + 2.0 * i / 100.0;
    // System.out.println(xx + "\t" + TEST_FUNCTION.evaluate(xx));
    // }

    // double[] x = new double[2];
    // for (int i = 0; i < 101; i++) {
    // x[0] = 0 + i * 10.0 / 100.0;
    // System.out.print("\t" + x[0]);
    // }
    // System.out.print("\n");
    // for (int i = 0; i < 101; i++) {
    // x[0] = -0. + i * 10 / 100.0;
    // System.out.print(x[0]);
    // for (int j = 0; j < 101; j++) {
    // x[1] = -0.0 + j * 10.0 / 100.0;
    // double y = COS_EXP_FUNCTION.evaluate(x);
    // System.out.print("\t" + y);
    // }
    // System.out.print("\n");
    // }

  }

  @Test
  public void testPerfectFit() {
    GeneralizedLeastSquare gls = new GeneralizedLeastSquare();
    LeastSquareResults results = gls.solve(X, Y, SIGMA, SIN_FUNCTIONS);
    assertEquals(0.0, results.getChiSq(), 1e-8);
    DoubleMatrix1D w = results.getParameters();
    for (int i = 0; i < WEIGHTS.length; i++) {
      assertEquals(WEIGHTS[i], w.getEntry(i), 1e-8);
    }
  }

  @Test
  public void testPerfectFitVector() {
    GeneralizedLeastSquare gls = new GeneralizedLeastSquare();
    LeastSquareResults results = gls.solve(X_TRIG, Y_TRIG, SIGMA_TRIG, VECTOR_TRIG_FUNCTIONS);
    assertEquals(0.0, results.getChiSq(), 1e-8);
    DoubleMatrix1D w = results.getParameters();
    for (int i = 0; i < WEIGHTS.length; i++) {
      assertEquals(WEIGHTS[i], w.getEntry(i), 1e-8);
    }
  }

  @Test
  public void testFit() {
    GeneralizedLeastSquare gls = new GeneralizedLeastSquare();
    double[] y = new double[Y.length];
    for (int i = 0; i < Y.length; i++) {
      y[i] = Y[i] + SIGMA[i] * NORMAL.nextRandom();
    }

    LeastSquareResults results = gls.solve(X, y, SIGMA, SIN_FUNCTIONS);
    assertTrue(results.getChiSq() < 3 * Y.length);

  }

  @Test
  public void testBSplineFit() {
    GeneralizedLeastSquare gls = new GeneralizedLeastSquare();

    LeastSquareResults results = gls.solve(X, Y, SIGMA, BASIS_FUNCTIONS);
    Function1D<Double, Double> spline = new BasisFunctionAggregation<Double>(BASIS_FUNCTIONS, results.getParameters().getData());
    assertEquals(0.0, results.getChiSq(), 1e-12);
    assertEquals(-0.023605293, spline.evaluate(0.5), 1e-8);

    if (PRINT) {
      System.out.println("Chi^2:\t" + results.getChiSq());
      System.out.println("weights:\t" + results.getParameters());

      for (int i = 0; i < 101; i++) {
        double x = 0 + i * 2.0 / 100.0;
        System.out.println(x + "\t" + spline.evaluate(x));
      }
      for (int i = 0; i < X.length; i++) {
        System.out.println(X[i] + "\t" + Y[i]);
      }
    }
  }

  @Test
  public void testBSplineFit2D() {
    GeneralizedLeastSquare gls = new GeneralizedLeastSquare();

    LeastSquareResults results = gls.solve(X_COS_EXP, Y_COS_EXP, SIGMA_COS_EXP, BASIS_FUNCTIONS_2D);
    Function1D<double[], Double> spline = new BasisFunctionAggregation<double[]>(BASIS_FUNCTIONS_2D, results.getParameters().getData());
    assertEquals(0.0, results.getChiSq(), 1e-25);
    assertEquals(0.05161579, spline.evaluate(new double[] {4, 3}), 1e-8);

    /*
     * Print out function for debugging
     */
    if (PRINT) {
      System.out.println("Chi^2:\t" + results.getChiSq());
      System.out.println("weights:\t" + results.getParameters());

      double[] x = new double[2];

      for (int i = 0; i < 101; i++) {
        x[0] = 0 + i * 10.0 / 100.0;
        System.out.print("\t" + x[0]);
      }
      System.out.print("\n");
      for (int i = 0; i < 101; i++) {
        x[0] = -0. + i * 10 / 100.0;
        System.out.print(x[0]);
        for (int j = 0; j < 101; j++) {
          x[1] = -0.0 + j * 10.0 / 100.0;
          double y = spline.evaluate(x);
          System.out.print("\t" + y);
        }
        System.out.print("\n");
      }
    }
  }

  @Test
  public void testPSplineFit() {
    GeneralizedLeastSquare gls = new GeneralizedLeastSquare();

    GeneralizedLeastSquareResults<Double> results = gls.solve(X, Y, SIGMA, BASIS_FUNCTIONS, 1000.0, 2);
    Function1D<Double, Double> spline = results.getFunction();
    assertEquals(2225.7, results.getChiSq(), 1e-1);
    assertEquals(-0.758963811327287, spline.evaluate(1.1), 1e-8);

    /*
     * Print out function for debugging
     */
    if (PRINT) {
      System.out.println("Chi^2:\t" + results.getChiSq());
      System.out.println("weights:\t" + results.getParameters());

      for (int i = 0; i < 101; i++) {
        double x = 0 + i * 2.0 / 100.0;
        System.out.println(x + "\t" + spline.evaluate(x));
      }
      for (int i = 0; i < X.length; i++) {
        System.out.println(X[i] + "\t" + Y[i]);
      }
    }
  }

  @Test
  public void testPSplineFit2D() {

    PSplineFitter psf = new PSplineFitter();
    GeneralizedLeastSquareResults<double[]> results = psf.solve(X_COS_EXP, Y_COS_EXP, SIGMA_COS_EXP, new double[] {0.0, 0.0}, new double[] {10.0, 10.0}, new int[] {10, 10}, new int[] {3, 3},
        new double[] {0.001, 0.001}, new int[] {3, 3});

    assertEquals(0.0, results.getChiSq(), 1e-9);
    Function1D<double[], Double> spline = results.getFunction();
    assertEquals(0.462288104, spline.evaluate(new double[] {4, 3}), 1e-8);

    /*
     * Print out function for debugging
     */
    if (PRINT) {
      System.out.println("Chi^2:\t" + results.getChiSq());
      System.out.println("weights:\t" + results.getParameters());

      double[] x = new double[2];

      for (int i = 0; i < 101; i++) {
        x[0] = 0 + i * 10.0 / 100.0;
        System.out.print("\t" + x[0]);
      }
      System.out.print("\n");
      for (int i = 0; i < 101; i++) {
        x[0] = -0. + i * 10 / 100.0;
        System.out.print(x[0]);
        for (int j = 0; j < 101; j++) {
          x[1] = -0.0 + j * 10.0 / 100.0;
          double y = spline.evaluate(x);
          System.out.print("\t" + y);
        }
        System.out.print("\n");
      }
    }
  }
}
