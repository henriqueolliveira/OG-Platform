/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries.object;


import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.opengamma.util.timeseries.ObjectTimeSeries;
import com.opengamma.util.timeseries.date.time.DateTimeObjectTimeSeries;
import com.opengamma.util.timeseries.date.time.ListDateTimeObjectTimeSeries;

public class ListDateTimeObjectTimeSeriesTest extends DateObjectTimeSeriesTest {

  @Override
  public DateTimeObjectTimeSeries<BigDecimal> createEmptyTimeSeries() {
    return new ListDateTimeObjectTimeSeries<BigDecimal>();
  }

  @Override
  public DateTimeObjectTimeSeries<BigDecimal> createTimeSeries(Date[] times, BigDecimal[] values) {
    return new ListDateTimeObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  public DateTimeObjectTimeSeries<BigDecimal> createTimeSeries(List<Date> times, List<BigDecimal> values) {
    return new ListDateTimeObjectTimeSeries<BigDecimal>(times, values);
  }

  @Override
  public ObjectTimeSeries<Date, BigDecimal> createTimeSeries(ObjectTimeSeries<Date, BigDecimal> dts) {
    return new ListDateTimeObjectTimeSeries<BigDecimal>(dts);
  }
}