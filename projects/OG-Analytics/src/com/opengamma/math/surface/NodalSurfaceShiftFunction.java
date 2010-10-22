/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.surface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class NodalSurfaceShiftFunction implements SurfaceShiftFunction<NodalDoublesSurface> {

  @Override
  public NodalDoublesSurface evaluate(final NodalDoublesSurface surface, final double shift) {
    Validate.notNull(surface, "surface");
    return evaluate(surface, shift, "PARALLEL_SHIFT_" + surface.getName());
  }

  @Override
  public NodalDoublesSurface evaluate(final NodalDoublesSurface surface, final double shift, final String newName) {
    Validate.notNull(surface, "surface");
    final double[] xData = surface.getXDataAsPrimitive();
    final double[] yData = surface.getYDataAsPrimitive();
    final double[] zData = surface.getZDataAsPrimitive();
    final int n = zData.length;
    final double[] shiftedZ = new double[n];
    for (int i = 0; i < n; i++) {
      shiftedZ[i] = zData[i] + shift;
    }
    return NodalDoublesSurface.from(xData, yData, shiftedZ, newName);
  }

  @Override
  public NodalDoublesSurface evaluate(final NodalDoublesSurface surface, final double x, final double y, final double shift) {
    Validate.notNull(surface, "surface");
    return evaluate(surface, x, y, shift, "SINGLE_SHIFT_" + surface.getName());
  }

  @Override
  public NodalDoublesSurface evaluate(final NodalDoublesSurface surface, final double x, final double y, final double shift, final String newName) {
    Validate.notNull(surface, "surface");
    final double[] xData = surface.getXDataAsPrimitive();
    final double[] yData = surface.getYDataAsPrimitive();
    final double[] zData = surface.getZDataAsPrimitive();
    final int n = zData.length;
    final List<Integer> indices = new ArrayList<Integer>();
    for (int i = 0; i < n; i++) {
      if (Double.doubleToLongBits(xData[i]) == Double.doubleToLongBits(x)) {
        indices.add(i);
      }
    }
    if (indices.isEmpty()) {
      throw new IllegalArgumentException("No x data in surface for value " + x);
    }
    final double[] shiftedZ = Arrays.copyOf(zData, n);
    for (final int i : indices) {
      if (Double.doubleToLongBits(yData[i]) == Double.doubleToLongBits(y)) {
        shiftedZ[i] += shift;
        return NodalDoublesSurface.from(xData, yData, shiftedZ, newName);
      }
    }
    throw new IllegalArgumentException("No x-y data in surface for (" + x + ", " + y + ")");
  }

  @Override
  public NodalDoublesSurface evaluate(final NodalDoublesSurface surface, final double[] xShift, final double[] yShift, final double[] zShift) {
    Validate.notNull(surface, "surface");
    return evaluate(surface, xShift, yShift, zShift, "MULTIPLE_SHIFT_" + surface.getName());
  }

  @Override
  public NodalDoublesSurface evaluate(final NodalDoublesSurface surface, final double[] xShift, final double[] yShift, final double[] shift, final String newName) {
    Validate.notNull(surface, "surface");
    Validate.notNull(xShift, "x shift");
    Validate.notNull(yShift, "y shift");
    Validate.notNull(shift, "shifts");
    Validate.isTrue(xShift.length == yShift.length && xShift.length == shift.length);
    final double[] xData = surface.getXDataAsPrimitive();
    final double[] yData = surface.getYDataAsPrimitive();
    final double[] zData = surface.getZDataAsPrimitive();
    final int n = zData.length;
    final double[] shiftedZ = Arrays.copyOf(zData, n);
    for (int i = 0; i < xShift.length; i++) {
      final double x = xShift[i];
      final List<Integer> indices = new ArrayList<Integer>();
      for (int j = 0; j < n; j++) {
        if (Double.doubleToLongBits(xData[j]) == Double.doubleToLongBits(x)) {
          indices.add(j);
        }
      }
      if (indices.isEmpty()) {
        throw new IllegalArgumentException("No x data in surface for value " + x);
      }
      boolean foundValue = false;
      for (final int index : indices) {
        if (Double.doubleToLongBits(yData[index]) == Double.doubleToLongBits(yShift[i])) {
          shiftedZ[index] += shift[i];
          foundValue = true;
        }
      }
      if (!foundValue) {
        throw new IllegalArgumentException("No x-y data in surface for (" + x + ", " + yShift[i] + ")");
      }
    }
    return NodalDoublesSurface.from(xData, yData, shiftedZ, newName);
  }

}