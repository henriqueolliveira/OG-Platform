/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import javax.time.calendar.LocalDate;

import com.opengamma.financial.convention.calendar.Calendar;

/**
 * The following business day convention.
 * <p>
 * This chooses the next working day following a non-working day.
 */
public class FollowingBusinessDayConvention extends AbstractBusinessDayConvention {

  @Override
  public LocalDate adjustDate(final Calendar workingDays, final LocalDate date) {
    LocalDate result = date;
    while (!workingDays.isWorkingDay(result)) {
      result = result.plusDays(1);
    }
    return result;
  }

  @Override
  public String getConventionName() {
    return "Following";
  }

}