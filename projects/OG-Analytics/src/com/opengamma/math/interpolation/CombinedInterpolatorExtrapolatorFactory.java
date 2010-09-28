/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
@SuppressWarnings("unchecked")
public final class CombinedInterpolatorExtrapolatorFactory {

  private CombinedInterpolatorExtrapolatorFactory() {
  }

  public static CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> getInterpolator(final String interpolatorName) {
    final Interpolator1D interpolator = Interpolator1DFactory.getInterpolator(interpolatorName);
    return new CombinedInterpolatorExtrapolator(interpolator);
  }

  public static CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> getInterpolator(final String interpolatorName, final String extrapolatorName) {
    final Interpolator1D interpolator = Interpolator1DFactory.getInterpolator(interpolatorName);
    if (extrapolatorName == null || extrapolatorName.isEmpty()) {
      return new CombinedInterpolatorExtrapolator(interpolator);
    }
    final Interpolator1D extrapolator = getExtrapolator(extrapolatorName, interpolator);
    return new CombinedInterpolatorExtrapolator(interpolator, extrapolator, extrapolator);
  }

  // REVIEW emcleod 4-8-2010 not sure if this is how people will want to construct the combined interpolator - should it be more strict?
  // Also see CombinedInterpolatorExtrapolatorNodeSensitivityCalculatorFactory
  public static CombinedInterpolatorExtrapolator<? extends Interpolator1DDataBundle> getInterpolator(final String interpolatorName, final String leftExtrapolatorName,
      final String rightExtrapolatorName) {
    final Interpolator1D interpolator = Interpolator1DFactory.getInterpolator(interpolatorName);
    if (leftExtrapolatorName == null || leftExtrapolatorName.isEmpty()) {
      if (rightExtrapolatorName == null || rightExtrapolatorName.isEmpty()) {
        return new CombinedInterpolatorExtrapolator(interpolator);
      }
      final Interpolator1D extrapolator = getExtrapolator(rightExtrapolatorName, interpolator);
      return new CombinedInterpolatorExtrapolator(interpolator, extrapolator);
    }
    if (rightExtrapolatorName == null || rightExtrapolatorName.isEmpty()) {
      final Interpolator1D extrapolator = getExtrapolator(leftExtrapolatorName, interpolator);
      return new CombinedInterpolatorExtrapolator(interpolator, extrapolator);
    }
    final Interpolator1D leftExtrapolator = getExtrapolator(leftExtrapolatorName, interpolator);
    final Interpolator1D rightExtrapolator = getExtrapolator(rightExtrapolatorName, interpolator);
    return new CombinedInterpolatorExtrapolator(interpolator, leftExtrapolator, rightExtrapolator);
  }

  private static Interpolator1D getExtrapolator(final String extrapolatorName, final Interpolator1D interpolator) {
    if (extrapolatorName.equals(Interpolator1DFactory.FLAT_EXTRAPOLATOR)) {
      return Interpolator1DFactory.FLAT_EXTRAPOLATOR_INSTANCE;
    } else if (extrapolatorName.equals(Interpolator1DFactory.LINEAR_EXTRAPOLATOR)) {
      return new LinearExtrapolator1D(interpolator);
    }
    throw new IllegalArgumentException("Cannot get extrapolator " + extrapolatorName);
  }
}