/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.core.security.Security;
import com.opengamma.id.Identifier;
import com.opengamma.util.time.Tenor;

/**
 *  
 */
public class FixedIncomeStripWithSecurity {
  private StripInstrumentType _instrumentType;
  private Tenor _tenor;
  private Tenor _resolvedTenor;
  private int _nthFutureFromTenor;
  private ZonedDateTime _maturity;
  private Identifier _securityIdentifier;
  private Security _security;

  /**
   * Gets the instrumentType field.
   * @return the instrumentType
   */
  public StripInstrumentType getInstrumentType() {
    return _instrumentType;
  }
  
  /**
   * Gets the tenor field
   * @return the tenor
   */
  public Tenor getTenor() {
    return _tenor;
  }
  
  /**
   * Gets the resolved tenor field
   * @return the tenor
   */
  public Tenor getResolvedTenor() {
    return _resolvedTenor;
  }
  
  /**
   * Get the number of the quarterly IR futures after the tenor to choose.  
   * NOTE: THIS DOESN'T REFER TO A GENERIC FUTURE
   * @return number of futures after the tenor
   * @throws IllegalStateException if called on a non-future strip
   */
  public int getNumberOfFuturesAfterTenor() {
    if (_instrumentType != StripInstrumentType.FUTURE) {
      throw new IllegalStateException("Cannot get number of futures after tenor for a non future strip " + toString());
    }
    return _nthFutureFromTenor;
  }
  
  /**
   * Gets the years field.
   * @return the years
   */
  public ZonedDateTime getMaturity() {
    return _maturity;
  }
  
  /**
   * Gets the identifier that was used to resolve the security
   * This is available, just so the same identifier can be conveniently used to retrieve requested market data.
   * @return The security identifier
   */
  public Identifier getSecurityIdentifier() {
    return _securityIdentifier;
  }

  /**
   * Gets the security field.
   * @return the security
   */
  public Security getSecurity() {
    return _security;
  }

  public FixedIncomeStripWithSecurity(StripInstrumentType instrumentType, Tenor originalTenor, 
                                      Tenor resolvedTenor, int nthFutureFromOriginalTenor, 
                                      ZonedDateTime maturity, Identifier securityIdentifier, 
                                      Security security) {
    Validate.isTrue(instrumentType == StripInstrumentType.FUTURE, "Trying to create a node with a nthFutureFromOriginalTenor param when not a future node");
    _instrumentType = instrumentType;
    _tenor = originalTenor;
    _resolvedTenor = resolvedTenor;
    _nthFutureFromTenor = nthFutureFromOriginalTenor;
    _maturity = maturity;
    _securityIdentifier = securityIdentifier;
    _security = security;
  }
  
  public FixedIncomeStripWithSecurity(StripInstrumentType instrumentType, Tenor originalTenor, 
      Tenor resolvedTenor, ZonedDateTime maturity, Identifier securityIdentifier, 
      Security security) {
    Validate.isTrue(instrumentType != StripInstrumentType.FUTURE, "Trying to create a node without a nthFutureFromOriginalTenor param when a future node");
    _instrumentType = instrumentType;
    _tenor = originalTenor;
    _resolvedTenor = resolvedTenor;
    _maturity = maturity;
    _securityIdentifier = securityIdentifier;
    _security = security;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof FixedIncomeStripWithSecurity) {
      FixedIncomeStripWithSecurity other = (FixedIncomeStripWithSecurity) obj;
      return ObjectUtils.equals(_tenor, other._tenor) &&
             ObjectUtils.equals(_nthFutureFromTenor, other._nthFutureFromTenor) &&
             ObjectUtils.equals(_maturity, other._maturity) &&
             ObjectUtils.equals(_security, other._security) &&
             _instrumentType == other._instrumentType;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _maturity.hashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  public int compareTo(FixedIncomeStripWithSecurity o) {
    int result = (int) getMaturity().compareTo(o.getMaturity());
    if (result != 0) {
      return result;
    }
    result = getInstrumentType().ordinal() - o.getInstrumentType().ordinal(); 
    if (result != 0) {
      return result;
    }
    result = getSecurity().getUniqueId().getValue().compareTo(o.getSecurity().getUniqueId().getValue());
    return result;
  }
}
