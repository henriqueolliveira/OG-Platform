/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.holiday;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;

import com.opengamma.financial.Currency;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;

/**
 * 
 */
public class DefaultHolidaySource implements HolidaySource {
  private HolidayMaster _holidayMaster;
  
  public DefaultHolidaySource(HolidayMaster holidayMaster) {
    _holidayMaster = holidayMaster;
  }
  
  private boolean isWeekend(LocalDate date) {
    if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean isHoliday(Currency currency, LocalDate holidayDate) {
    HolidaySearchRequest request = new HolidaySearchRequest(currency, holidayDate);
    return _holidayMaster.searchHolidays(request).isHoliday() || isWeekend(holidayDate);
  }

  @Override
  public boolean isHoliday(IdentifierBundle regionOrExchangeIds, LocalDate holidayDate, HolidayType holidayType) {
    HolidaySearchRequest request = new HolidaySearchRequest(regionOrExchangeIds, holidayType, holidayDate);
    return _holidayMaster.searchHolidays(request).isHoliday() || isWeekend(holidayDate);
  }
  
  @Override
  public boolean isHoliday(Identifier regionOrExchangeId, LocalDate holidayDate, HolidayType holidayType) {
    HolidaySearchRequest request = new HolidaySearchRequest(regionOrExchangeId, holidayType, holidayDate);
    return _holidayMaster.searchHolidays(request).isHoliday() || isWeekend(holidayDate);
  }
 
}