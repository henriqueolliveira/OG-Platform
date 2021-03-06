/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.schedule;

import static org.testng.AssertJUnit.assertArrayEquals;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.schedule.AnnualScheduleCalculator;
import com.opengamma.financial.schedule.Schedule;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class AnnualScheduleCalculatorTest extends ScheduleCalculatorTestCase {
  private static final AnnualScheduleCalculator CALCULATOR = new AnnualScheduleCalculator();

  @Override
  public Schedule getScheduleCalculator() {
    return CALCULATOR;
  }

  @Test
  public void testStartAndEndSame1() {
    final LocalDate date = LocalDate.of(2001, 2, 13);
    final LocalDate[] dates = CALCULATOR.getSchedule(date, date, false, true);
    assertEquals(dates.length, 1);
    assertEquals(dates[0], date);
  }

  @Test
  public void testStartAndEndSame2() {
    final ZonedDateTime date = DateUtil.getUTCDate(2001, 2, 13);
    final ZonedDateTime[] dates = CALCULATOR.getSchedule(date, date, false, true);
    assertEquals(dates.length, 1);
    assertEquals(dates[0], date);
  }

  @Test
  public void test1() {
    LocalDate startDate = LocalDate.of(2000, 1, 1);
    LocalDate endDate = LocalDate.of(2000, 12, 30);
    LocalDate[] forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    LocalDate[] backward = CALCULATOR.getSchedule(startDate, endDate, true, true);
    assertEquals(forward.length, 1);
    assertEquals(backward.length, 1);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], endDate);
    startDate = LocalDate.of(2000, 1, 1);
    endDate = LocalDate.of(2010, 2, 9);
    final int years = 11;
    forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    backward = CALCULATOR.getSchedule(startDate, endDate, true, true);
    assertEquals(forward.length, years);
    assertEquals(backward.length, years);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], LocalDate.of(2000, 2, 9));
    assertEquals(forward[years - 1], LocalDate.of(2010, 1, 1));
    assertEquals(backward[years - 1], endDate);
    for (int i = 1; i < years; i++) {
      assertEquals(forward[i].getYear() - forward[i - 1].getYear(), 1);
      assertEquals(forward[i].getMonthOfYear(), startDate.getMonthOfYear());
      assertEquals(forward[i].getDayOfMonth(), startDate.getDayOfMonth());
      assertEquals(backward[i].getYear() - backward[i - 1].getYear(), 1);
      assertEquals(backward[i].getMonthOfYear(), endDate.getMonthOfYear());
      assertEquals(backward[i].getDayOfMonth(), endDate.getDayOfMonth());
    }
  }

  @Test
  public void test2() {
    ZonedDateTime startDate = DateUtil.getUTCDate(2000, 1, 1);
    ZonedDateTime endDate = DateUtil.getUTCDate(2000, 12, 30);
    ZonedDateTime[] forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    ZonedDateTime[] backward = CALCULATOR.getSchedule(startDate, endDate, true, true);
    assertEquals(forward.length, 1);
    assertEquals(backward.length, 1);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], endDate);
    startDate = DateUtil.getUTCDate(2000, 1, 1);
    endDate = DateUtil.getUTCDate(2010, 2, 9);
    final int years = 11;
    forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    backward = CALCULATOR.getSchedule(startDate, endDate, true, true);
    assertEquals(forward.length, years);
    assertEquals(backward.length, years);
    assertEquals(forward[0], startDate);
    assertEquals(backward[0], DateUtil.getUTCDate(2000, 2, 9));
    assertEquals(forward[years - 1], DateUtil.getUTCDate(2010, 1, 1));
    assertEquals(backward[years - 1], endDate);
    for (int i = 1; i < years; i++) {
      assertEquals(forward[i].getYear() - forward[i - 1].getYear(), 1);
      assertEquals(forward[i].getMonthOfYear(), startDate.getMonthOfYear());
      assertEquals(forward[i].getDayOfMonth(), startDate.getDayOfMonth());
      assertEquals(backward[i].getYear() - backward[i - 1].getYear(), 1);
      assertEquals(backward[i].getMonthOfYear(), endDate.getMonthOfYear());
      assertEquals(backward[i].getDayOfMonth(), endDate.getDayOfMonth());
    }
  }

  @Test
  public void testEndOfMonth1() {
    final LocalDate startDate = LocalDate.of(2000, 2, 29);
    final LocalDate endDate = LocalDate.of(2008, 2, 29);
    final LocalDate[] forward = CALCULATOR.getSchedule(startDate, endDate, true, false);
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, false, false));
    final LocalDate[] recursive = new LocalDate[] {LocalDate.of(2000, 2, 29), LocalDate.of(2001, 2, 28), LocalDate.of(2002, 2, 28), LocalDate.of(2003, 2, 28), LocalDate.of(2004, 2, 28),
        LocalDate.of(2005, 2, 28), LocalDate.of(2006, 2, 28), LocalDate.of(2007, 2, 28), LocalDate.of(2008, 2, 28)};
    final LocalDate[] nonRecursive = new LocalDate[] {LocalDate.of(2000, 2, 29), LocalDate.of(2001, 2, 28), LocalDate.of(2002, 2, 28), LocalDate.of(2003, 2, 28), LocalDate.of(2004, 2, 29),
        LocalDate.of(2005, 2, 28), LocalDate.of(2006, 2, 28), LocalDate.of(2007, 2, 28), LocalDate.of(2008, 2, 29)};
    assertArrayEquals(forward, nonRecursive);
    assertArrayEquals(CALCULATOR.getSchedule(startDate, endDate, false, true), recursive);
  }

  @Test
  public void testEndOfMonth2() {
    final ZonedDateTime startDate = DateUtil.getUTCDate(2000, 2, 29);
    final ZonedDateTime endDate = DateUtil.getUTCDate(2008, 2, 29);
    final ZonedDateTime[] forward = CALCULATOR.getSchedule(startDate, endDate, true, false);
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, false, false));
    final ZonedDateTime[] recursive = new ZonedDateTime[] {DateUtil.getUTCDate(2000, 2, 29), DateUtil.getUTCDate(2001, 2, 28), DateUtil.getUTCDate(2002, 2, 28), DateUtil.getUTCDate(2003, 2, 28),
        DateUtil.getUTCDate(2004, 2, 28),
        DateUtil.getUTCDate(2005, 2, 28), DateUtil.getUTCDate(2006, 2, 28), DateUtil.getUTCDate(2007, 2, 28), DateUtil.getUTCDate(2008, 2, 28)};
    final ZonedDateTime[] nonRecursive = new ZonedDateTime[] {DateUtil.getUTCDate(2000, 2, 29), DateUtil.getUTCDate(2001, 2, 28), DateUtil.getUTCDate(2002, 2, 28), DateUtil.getUTCDate(2003, 2, 28),
        DateUtil.getUTCDate(2004, 2, 29),
        DateUtil.getUTCDate(2005, 2, 28), DateUtil.getUTCDate(2006, 2, 28), DateUtil.getUTCDate(2007, 2, 28), DateUtil.getUTCDate(2008, 2, 29)};
    assertArrayEquals(forward, nonRecursive);
    assertArrayEquals(CALCULATOR.getSchedule(startDate, endDate, false, true), recursive);
  }
}
