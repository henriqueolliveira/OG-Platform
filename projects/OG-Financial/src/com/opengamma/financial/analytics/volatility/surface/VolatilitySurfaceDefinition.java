/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.util.Arrays;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.opengamma.util.money.Currency;

/**
 * Hold the valid range of X and Y for a surface.  E.g. VolatilitySurfaceDefinition<Tenor, Double> (tenor vs strike, z is volatility), where Tenors go from 1YR..10YR, 
 * strikes from 220.0d to 240.0d with deltas of 5
 * @param <X> Type of the x-data
 * @param <Y> Type of the y-data
 */
public class VolatilitySurfaceDefinition<X, Y> {
  private String _name;
  private Currency _currency;
  private String _interpolatorName;
  private X[] _xs;
  private Y[] _ys;
    
  public VolatilitySurfaceDefinition(String name, Currency currency, String interpolatorName, X[] xs, Y[] ys) {
    Validate.notNull(name, "Name");
    Validate.notNull(currency, "Currency");
    Validate.notNull(interpolatorName, "Interpolator Name");
    Validate.notNull(xs, "xs");
    Validate.notNull(ys, "ys");
    _name = name;
    _currency = currency;
    _interpolatorName = interpolatorName;
    _xs = xs;
    _ys = ys;
  }
  
  public X[] getXs() {
    return _xs;
  }
  
  public Y[] getYs() {
    return _ys;
  }
  
  public String getName() {
    return _name;
  }
  
  public Currency getCurrency() {
    return _currency;
  }
  
  public String getInterpolatorName() {
    return _interpolatorName;
  }
  
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof VolatilitySurfaceDefinition)) {
      return false;
    }
    VolatilitySurfaceDefinition<?, ?> other = (VolatilitySurfaceDefinition<?, ?>) o;
    return other.getCurrency().equals(getCurrency()) &&
           other.getName().equals(getName()) &&
           other.getInterpolatorName().equals(getInterpolatorName()) &&
           Arrays.equals(other.getXs(), getXs()) &&
           Arrays.equals(other.getYs(), getYs());
  }
  
  public int hashCode() {
    return getCurrency().hashCode() * getName().hashCode();
  }
  
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
