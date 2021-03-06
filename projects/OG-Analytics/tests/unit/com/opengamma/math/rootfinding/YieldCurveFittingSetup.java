/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;

import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderDataBundle;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderFunction;
import com.opengamma.financial.interestrate.MultipleYieldCurveFinderJacobian;
import com.opengamma.financial.interestrate.RateReplacingInterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponIbor;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.financial.interestrate.swap.definition.FixedFloatSwap;
import com.opengamma.financial.interestrate.swap.definition.TenorSwap;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.differentiation.VectorFieldFirstOrderDifferentiator;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.sensitivity.Interpolator1DNodeSensitivityCalculator;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.rootfinding.YieldCurveFittingTestDataBundle.TestType;
import com.opengamma.math.rootfinding.newton.NewtonVectorRootFinder;
import com.opengamma.util.money.Currency;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public abstract class YieldCurveFittingSetup {
  /** Random number generator */
  protected static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister64.DEFAULT_SEED);
  /** Replaces rates */
  protected static final RateReplacingInterestRateDerivativeVisitor REPLACE_RATE = RateReplacingInterestRateDerivativeVisitor.getInstance();

  /** Accuracy */
  protected static final double EPS = 1e-8;
  /** Number of steps */
  protected static final int STEPS = 100;
  private static final Currency CUR = Currency.USD;

  protected abstract Logger getLogger();

  protected abstract int getWarmupCycles();

  protected abstract int getBenchmarkCycles();

  protected YieldCurveFittingTestDataBundle getYieldCurveFittingTestDataBundle(final List<InterestRateDerivative> instruments, final YieldCurveBundle knownCurves, final List<String> curveNames,
      final List<double[]> curvesKnots, final Interpolator1D<? extends Interpolator1DDataBundle> extrapolator,
      final Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle> extrapolatorWithSense, final InterestRateDerivativeVisitor<YieldCurveBundle, Double> marketValueCalculator,
      final InterestRateDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> marketValueSensitivityCalculator, final double[] marketRates, final DoubleMatrix1D startPosition,
      final List<double[]> curveYields) {

    Validate.notNull(curveNames);
    Validate.notNull(curvesKnots);
    Validate.notNull(instruments);
    Validate.notNull(extrapolator);
    Validate.notNull(extrapolatorWithSense);

    final int n = curveNames.size();
    Validate.isTrue(n == curvesKnots.size());
    int count = 0;
    for (int i = 0; i < n; i++) {
      Validate.notNull(curvesKnots.get(i));
      count += curvesKnots.get(i).length;
    }
    Validate.isTrue(count <= instruments.size(), "more nodes than instruments");

    final LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>> unknownCurveInterpolators = new LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>>();
    final LinkedHashMap<String, double[]> unknownCurveNodes = new LinkedHashMap<String, double[]>();
    final LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>> unknownCurveNodeSensitivityCalculators = 
      new LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>>();

    for (int i = 0; i < n; i++) {
      unknownCurveInterpolators.put(curveNames.get(i), extrapolator);
      unknownCurveNodes.put(curveNames.get(i), curvesKnots.get(i));
      unknownCurveNodeSensitivityCalculators.put(curveNames.get(i), extrapolatorWithSense);
    }
    if (curveYields == null) {
      return new YieldCurveFittingTestDataBundle(instruments, knownCurves, unknownCurveNodes, unknownCurveInterpolators, unknownCurveNodeSensitivityCalculators, marketValueCalculator,
          marketValueSensitivityCalculator, marketRates, startPosition);
    }

    Validate.isTrue(curveYields.size() == n, "wrong number of true yields");
    final HashMap<String, double[]> yields = new HashMap<String, double[]>();
    for (int i = 0; i < n; i++) {
      yields.put(curveNames.get(i), curveYields.get(i));
    }
    return new YieldCurveFittingTestDataBundle(instruments, knownCurves, unknownCurveNodes, unknownCurveInterpolators, unknownCurveNodeSensitivityCalculators, marketValueCalculator,
        marketValueSensitivityCalculator, marketRates, startPosition, yields);
  }

  public void doHotSpot(final NewtonVectorRootFinder rootFinder, final YieldCurveFittingTestDataBundle data, final String name) {
    for (int i = 0; i < getWarmupCycles(); i++) {
      doTestForCurveFinding(rootFinder, data);

    }
    if (getBenchmarkCycles() > 0) {
      final OperationTimer timer = new OperationTimer(getLogger(), "processing {} cycles on " + name, getBenchmarkCycles());
      for (int i = 0; i < getBenchmarkCycles(); i++) {
        doTestForCurveFinding(rootFinder, data);
      }
      timer.finished();
    }
  }

  private void doTestForCurveFinding(final NewtonVectorRootFinder rootFinder, final YieldCurveFittingTestDataBundle data) {

    final Function1D<DoubleMatrix1D, DoubleMatrix1D> func = new MultipleYieldCurveFinderFunction(data, data.getMarketValueCalculator());
    Function1D<DoubleMatrix1D, DoubleMatrix2D> jac = null;

    if (data.getTestType() == TestType.ANALYTIC_JACOBIAN) {
      jac = new MultipleYieldCurveFinderJacobian(data, data.getMarketValueSensitivityCalculator());
    } else if (data.getTestType() == TestType.FD_JACOBIAN) {
      final VectorFieldFirstOrderDifferentiator fdJacCalculator = new VectorFieldFirstOrderDifferentiator();
      jac = fdJacCalculator.differentiate(func);
    } else {
      throw new IllegalArgumentException("unknown TestType " + data.getTestType());

    }

    final DoubleMatrix1D yieldCurveNodes = rootFinder.getRoot(func, jac, data.getStartPosition());
    final DoubleMatrix1D modelMarketValueDiff = func.evaluate(yieldCurveNodes);

    for (int i = 0; i < modelMarketValueDiff.getNumberOfElements(); i++) {
      assertEquals(0.0, modelMarketValueDiff.getEntry(i), EPS);
    }

    checkResult(yieldCurveNodes, data);
  }

  protected void checkResult(final DoubleMatrix1D yieldCurveNodes, final YieldCurveFittingTestDataBundle data) {
    final HashMap<String, double[]> yields = unpackYieldVector(data, yieldCurveNodes);

    final YieldCurveBundle bundle = new YieldCurveBundle();
    for (final String name : data.getCurveNames()) {
      final YieldAndDiscountCurve curve = makeYieldCurve(yields.get(name), data.getCurveNodePointsForCurve(name), data.getInterpolatorForCurve(name));
      bundle.setCurve(name, curve);
    }
    if (data.getKnownCurves() != null) {
      bundle.addAll(data.getKnownCurves());
    }

    // this is possibly a redundant test, as the very fact that
    // the root finder converged (and modelMarketValueDiff are within EPS of 0) means this will also pass
    for (int i = 0; i < data.getMarketRates().length; i++) {
      assertEquals(data.getMarketRates()[i], data.getMarketValueCalculator().visit(data.getDerivative(i), bundle), EPS);
    }

    // this test cannot be performed when we don't know what the true yield curves are - i.e. we start from market data
    if (data.getCurveYields() != null) {
      for (final String name : data.getCurveNames()) {
        final double[] trueYields = data.getCurveYields().get(name);
        final double[] fittedYields = yields.get(name);
        for (int i = 0; i < trueYields.length; i++) {
          assertEquals(trueYields[i], fittedYields[i], EPS);
        }
      }
    }
  }

  protected HashMap<String, double[]> unpackYieldVector(final YieldCurveFittingTestDataBundle data, final DoubleMatrix1D yieldCurveNodes) {

    final HashMap<String, double[]> res = new HashMap<String, double[]>();
    int start = 0;
    int end = 0;
    for (final String name : data.getCurveNames()) {
      end += data.getCurveNodePointsForCurve(name).length;
      final double[] temp = Arrays.copyOfRange(yieldCurveNodes.getData(), start, end);
      res.put(name, temp);
      start = end;
    }

    return res;
  }

  protected void assertJacobian(final YieldCurveFittingTestDataBundle data) {
    final MultipleYieldCurveFinderFunction func = new MultipleYieldCurveFinderFunction(data, data.getMarketValueCalculator());
    final MultipleYieldCurveFinderJacobian jac = new MultipleYieldCurveFinderJacobian(data, data.getMarketValueSensitivityCalculator());
    final VectorFieldFirstOrderDifferentiator fdCal = new VectorFieldFirstOrderDifferentiator();
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianFD = fdCal.differentiate(func);
    final DoubleMatrix2D jacExact = jac.evaluate(data.getStartPosition());
    final DoubleMatrix2D jacFD = jacobianFD.evaluate(data.getStartPosition());
    assertMatrixEquals(jacExact, jacFD, 1e-5);

  }

  protected static YieldAndDiscountCurve makeYieldCurve(final double[] yields, final double[] times, final Interpolator1D<? extends Interpolator1DDataBundle> interpolator) {
    final int n = yields.length;
    if (n != times.length) {
      throw new IllegalArgumentException("rates and times different lengths");
    }
    return new YieldCurve(InterpolatedDoublesCurve.from(times, yields, interpolator));
  }

  protected static MultipleYieldCurveFinderDataBundle updateInstruments(final MultipleYieldCurveFinderDataBundle old, final List<InterestRateDerivative> instruments, final double[] marketRates) {
    Validate.isTrue(instruments.size() == marketRates.length);
    return new MultipleYieldCurveFinderDataBundle(instruments, marketRates, old.getKnownCurves(), old.getUnknownCurveNodePoints(), old.getUnknownCurveInterpolators(),
        old.getUnknownCurveNodeSensitivityCalculators());
  }

  protected static InterestRateDerivative makeIRD(final String type, final double maturity, final String fundCurveName, final String indexCurveName, final double rate) {
    if ("cash".equals(type)) {
      return makeCash(maturity, fundCurveName, rate);
    } else if ("libor".equals(type)) {
      return makeLibor(maturity, indexCurveName, rate);
    } else if ("fra".equals(type)) {
      return makeFRA(maturity, fundCurveName, indexCurveName, rate);
    } else if ("future".equals(type)) {
      return makeFutrure(maturity, indexCurveName, rate);
    } else if ("swap".equals(type)) {
      return makeSwap(maturity, fundCurveName, indexCurveName, rate);
    } else if ("basisSwap".equals(type)) {
      return makeBasisSwap(maturity, fundCurveName, indexCurveName, rate);
    }
    throw new IllegalArgumentException("unknown IRD type " + type);
  }

  protected static InterestRateDerivative makeCash(final double time, final String fundCurveName, final double rate) {
    return new Cash(time, rate, fundCurveName);
  }

  protected static InterestRateDerivative makeLibor(final double time, final String indexCurveName, final double rate) {
    return new Cash(time, rate, indexCurveName);
  }

  protected static InterestRateDerivative makeFRA(final double time, final String fundCurveName, final String indexCurveName, final double rate) {
    return new ForwardRateAgreement(time - 0.25, time, rate, fundCurveName, indexCurveName);
  }

  protected static InterestRateDerivative makeFutrure(final double time, final String indexCurveName, final double rate) {
    return new InterestRateFuture(time, time + 0.25, 0.25, rate, indexCurveName);
  }

  protected static FixedFloatSwap makeSwap(final double time, final String fundCurveName, final String liborCurveName, final double rate) {
    final int index = (int) Math.round(2 * time);
    return makeSwap(index, fundCurveName, liborCurveName, rate);
  }

  protected static TenorSwap<CouponIbor> makeBasisSwap(final double time, final String fundCurveName, final String liborCurveName, final double rate) {

    final int index = (int) Math.round(4 * time);
    final double[] paymentTimes = new double[index];

    final double[] spreads = new double[index];
    final double[] indexFixing = new double[index];
    final double[] indexMaturity = new double[index];
    final double[] yearFracs = new double[index];
    for (int i = 0; i < index; i++) {
      indexFixing[i] = 0.25 * i;
      paymentTimes[i] = 0.25 * (i + 1);
      indexMaturity[i] = paymentTimes[i];
      spreads[i] = rate;
      yearFracs[i] = 0.25;
    }
    final GenericAnnuity<CouponIbor> payLeg = new AnnuityCouponIbor(CUR, paymentTimes, 1.0, fundCurveName, fundCurveName, true);
    final GenericAnnuity<CouponIbor> receiveLeg = new AnnuityCouponIbor(CUR, paymentTimes, indexFixing, indexFixing, indexMaturity, yearFracs, yearFracs, spreads, 1.0, fundCurveName, liborCurveName,
        false);
    return new TenorSwap<CouponIbor>(payLeg, receiveLeg);
  }

  /**
   * 
   * @param payments number of payments
   * @param fundingCurveName funding curve name
   * @param liborCurveName libor curve name
   * @param rate interest rate
   * @return A fixed-float swap
   */
  protected static FixedFloatSwap makeSwap(final int payments, final String fundingCurveName, final String liborCurveName, final double rate) {

    final double[] fixed = new double[payments];
    final double[] floating = new double[2 * payments];
    final double[] indexFixing = new double[2 * payments];
    final double[] indexMaturity = new double[2 * payments];
    final double[] yearFrac = new double[2 * payments];

    final double sigma = 0.0 / 365.0;

    for (int i = 0; i < payments; i++) {
      fixed[i] = 0.5 * (1 + i) + sigma * (RANDOM.nextDouble() - 0.5);
      floating[2 * i + 1] = fixed[i];
    }
    for (int i = 0; i < 2 * payments; i++) {
      if (i % 2 == 0) {
        floating[i] = 0.25 * (1 + i) + sigma * (RANDOM.nextDouble() - 0.5);
      }
      yearFrac[i] = 0.25 + sigma * (RANDOM.nextDouble() - 0.5);
      indexFixing[i] = 0.25 * i + sigma * (i == 0 ? RANDOM.nextDouble() / 2 : (RANDOM.nextDouble() - 0.5));
      indexMaturity[i] = 0.25 * (1 + i) + sigma * (RANDOM.nextDouble() - 0.5);
    }
    final AnnuityCouponFixed fixedLeg = new AnnuityCouponFixed(CUR, fixed, rate, fundingCurveName, true);

    final AnnuityCouponIbor floatingLeg = new AnnuityCouponIbor(CUR, floating, indexFixing, indexMaturity, yearFrac, 1.0, fundingCurveName, liborCurveName, false);
    return new FixedFloatSwap(fixedLeg, floatingLeg);
  }

  protected static Bond makeBond(final double maturity, final String curveName, final double coupon) {

    final int n = (int) Math.ceil(maturity * 2.0);
    final double[] paymentTimes = new double[n];
    paymentTimes[n - 1] = maturity;
    for (int i = n - 2; i >= 0; i--) {
      paymentTimes[i] = paymentTimes[i + 1] - 0.5;
    }
    final double accuredInterest = coupon * (0.5 - paymentTimes[0]);

    return new Bond(CUR, paymentTimes, coupon, 0.5, accuredInterest, curveName);
  }

  protected void assertMatrixEquals(final DoubleMatrix2D m1, final DoubleMatrix2D m2, final double eps) {
    final int m = m1.getNumberOfRows();
    final int n = m1.getNumberOfColumns();
    assertEquals(m2.getNumberOfRows(), m);
    assertEquals(m2.getNumberOfColumns(), n);
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < m; j++) {
        assertEquals(m1.getEntry(i, j), m2.getEntry(i, j), eps);
      }
    }
  }

}
