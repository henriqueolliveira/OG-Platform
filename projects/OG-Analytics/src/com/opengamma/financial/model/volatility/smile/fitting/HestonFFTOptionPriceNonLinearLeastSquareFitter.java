/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

import java.util.BitSet;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackPriceFunction;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.option.pricing.fourier.CharacteristicExponent;
import com.opengamma.financial.model.option.pricing.fourier.FFTPricer;
import com.opengamma.financial.model.option.pricing.fourier.HestonCharacteristicExponent;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.linearalgebra.DecompositionFactory;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.MatrixAlgebraFactory;
import com.opengamma.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.math.minimization.ParameterLimitsTransform;
import com.opengamma.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.math.minimization.SingleRangeLimitTransform;
import com.opengamma.math.minimization.TransformParameters;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.math.statistics.leastsquare.NonLinearLeastSquare;
import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class HestonFFTOptionPriceNonLinearLeastSquareFitter extends LeastSquareSmileFitter {
  private static final double DEFAULT_ALPHA = -0.5;
  private static final double DEFAULT_LIMIT_TOLERANCE = 1e-8;
  private static final NonLinearLeastSquare SOLVER = new NonLinearLeastSquare(DecompositionFactory.SV_COLT, MatrixAlgebraFactory.OG_ALGEBRA, 1e-4);
  private static final BlackPriceFunction BLACK_PRICE_FUNCTION = new BlackPriceFunction();
  private static final FFTPricer FFT_PRICER = new FFTPricer();
  private static final int N_PARAMETERS = 5;
  private static final ParameterLimitsTransform[] TRANSFORMS;
  private final Interpolator1D<Interpolator1DDataBundle> _interpolator;
  private final double _alpha;
  private final double _limitTolerance;

  static {
    TRANSFORMS = new ParameterLimitsTransform[N_PARAMETERS];
    TRANSFORMS[0] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // kappa > 0
    TRANSFORMS[1] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // theta > 0
    TRANSFORMS[2] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // vol0 > 0
    TRANSFORMS[3] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // omega > 0
    TRANSFORMS[4] = new DoubleRangeLimitTransform(-1.0, 1.0); // -1 <= rho <= 1
  }

  public HestonFFTOptionPriceNonLinearLeastSquareFitter() {
    this(Interpolator1DFactory.getInterpolator("DoubleQuadratic"), DEFAULT_ALPHA, DEFAULT_LIMIT_TOLERANCE);
  }

  public HestonFFTOptionPriceNonLinearLeastSquareFitter(final Interpolator1D<Interpolator1DDataBundle> interpolator) {
    this(interpolator, DEFAULT_ALPHA, DEFAULT_LIMIT_TOLERANCE);
  }

  public HestonFFTOptionPriceNonLinearLeastSquareFitter(final Interpolator1D<Interpolator1DDataBundle> interpolator, final double alpha, final double limitTolerance) {
    Validate.notNull(interpolator, "interpolator");
    Validate.isTrue(alpha != 0 && alpha != -1, "alpha cannot be 0 or -1");
    Validate.isTrue(limitTolerance > 0, "limit tolerance must be > 0");
    _interpolator = interpolator;
    _alpha = alpha;
    _limitTolerance = limitTolerance;
  }

  @Override
  public LeastSquareResults getFitResult(final EuropeanVanillaOption[] options, final BlackFunctionData[] data, final double[] initialFitParameters, final BitSet fixed) {
    return getFitResult(options, data, null, initialFitParameters, fixed);
  }

  @Override
  public LeastSquareResults getFitResult(final EuropeanVanillaOption[] options, final BlackFunctionData[] data, final double[] errors, final double[] initialFitParameters, final BitSet fixed) {
    testData(options, data, errors, initialFitParameters, fixed, N_PARAMETERS);
    final int n = options.length;
    final double[] strikes = new double[n];
    final double[] prices = new double[n];
    final double maturity = options[0].getTimeToExpiry();
    final double forward = data[0].getForward();
    strikes[0] = options[0].getStrike();
    prices[0] = BLACK_PRICE_FUNCTION.getPriceFunction(options[0]).evaluate(data[0]);
    for (int i = 1; i < n; i++) {
      Validate.isTrue(CompareUtils.closeEquals(options[i].getTimeToExpiry(), maturity),
          "All options must have the same maturity " + maturity + "; have one with maturity " + options[i].getTimeToExpiry());
      strikes[i] = options[i].getStrike();
      prices[i] = BLACK_PRICE_FUNCTION.getPriceFunction(options[i]).evaluate(data[i]);
    }
    final TransformParameters transforms = new TransformParameters(new DoubleMatrix1D(initialFitParameters), TRANSFORMS, fixed);
    final double lowStrike = strikes[0];
    final double highStrike = strikes[n - 1];
    final double limitSigma = (data[0].getBlackVolatility() + data[n - 1].getBlackVolatility()) / 2.0;
    final BlackFunctionData blackData = new BlackFunctionData(forward, 1.0, limitSigma);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strikes[0], maturity, true);
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> hestonVols = new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D fp) {
        final DoubleMatrix1D mp = transforms.inverseTransform(fp);
        final double kappa = mp.getEntry(0);
        final double theta = mp.getEntry(1);
        final double vol0 = mp.getEntry(2);
        final double omega = mp.getEntry(3);
        final double rho = mp.getEntry(4);
        final CharacteristicExponent ce = new HestonCharacteristicExponent(kappa, theta, vol0, omega, rho);
        final double[][] strikeNPrice = FFT_PRICER.price(blackData, option, ce, lowStrike, highStrike, n, _alpha, _limitTolerance);
        final int nStrikes = strikeNPrice.length;
        final double[] k = new double[nStrikes];
        final double[] price = new double[nStrikes];
        for (int i = 0; i < nStrikes; i++) {
          k[i] = strikeNPrice[i][0];
          price[i] = strikeNPrice[i][1];
        }
        final Interpolator1DDataBundle dataBundle = _interpolator.getDataBundle(k, price);
        final int m = strikes.length;
        final double[] res = new double[m];
        for (int i = 0; i < m; i++) {
          res[i] = _interpolator.interpolate(dataBundle, strikes[i]);
        }
        return new DoubleMatrix1D(res);
      }
    };
    final DoubleMatrix1D fp = transforms.transform(new DoubleMatrix1D(initialFitParameters));
    final LeastSquareResults results = errors == null ? SOLVER.solve(new DoubleMatrix1D(prices), hestonVols, fp) : SOLVER.solve(new DoubleMatrix1D(prices), new DoubleMatrix1D(errors), hestonVols, fp);
    return new LeastSquareResults(results.getChiSq(), transforms.inverseTransform(results.getParameters()), new DoubleMatrix2D(new double[N_PARAMETERS][N_PARAMETERS]));
  }
}
