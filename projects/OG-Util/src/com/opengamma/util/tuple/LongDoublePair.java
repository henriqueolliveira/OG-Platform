/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.tuple;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

/**
 * An immutable pair consisting of an {@code long} and {@code double}.
 * <p>
 * The class provides direct access to the primitive types and implements
 * the relevant fastutil interface.
 */
public class LongDoublePair extends Pair<Long, Double> implements Long2DoubleMap.Entry {

  /** The first element. */
  public final long first;  // CSIGNORE
  /** The second element. */
  public final double second;  // CSIGNORE

  /**
   * Constructor.
   * @param first  the first element
   * @param second  the second element
   */
  public LongDoublePair(final long first, final double second) {
    this.first = first;
    this.second = second;
  }

  //-------------------------------------------------------------------------
  @Override
  public Long getFirst() {
    return first;
  }

  @Override
  public Double getSecond() {
    return second;
  }

  /**
   * Gets the first element as a primitive {@code long}.
   * @return the primitive
   */
  public long getFirstLong() {
    return first;
  }

  /**
   * Gets the second element as a primitive {@code double}.
   * @return the primitive
   */
  public double getSecondDouble() {
    return second;
  }

  //-------------------------------------------------------------------------
  @Override
  public long getLongKey() {
    return first;
  }

  @Override
  public double getDoubleValue() {
    return second;
  }

  @Override
  public double setValue(final double value) {
    throw new UnsupportedOperationException("Immutable");
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof LongDoublePair) {
      final LongDoublePair other = (LongDoublePair) obj;
      return this.first == other.first && this.second == other.second;
    }
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    // see Map.Entry API specification
    final long s = Double.doubleToLongBits(second);
    return ((int) (first ^ (first >>> 32))) ^ ((int) (s ^ (s >>> 32)));
  }

}