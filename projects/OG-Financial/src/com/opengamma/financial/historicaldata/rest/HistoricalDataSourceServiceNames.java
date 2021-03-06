/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.historicaldata.rest;

import com.opengamma.util.rest.AbstractResourceService;

/**
 * 
 */
public final class HistoricalDataSourceServiceNames {

  private HistoricalDataSourceServiceNames() {
  }

  /**
   * 
   */
  public static final String NULL_VALUE = "null";

  /**
   *
   */
  public static final String DEFAULT_HISTORICALDATASOURCE_NAME = AbstractResourceService.DEFAULT_RESOURCE_NAME;

  /**
  *
  */
  public static final String HISTORICALDATASOURCE_UNIQUEID = "uniqueId";
  /**
  *
  */
  public static final String HISTORICALDATASOURCE_TIMESERIES = "timeSeries";

  /**
   * 
   */
  public static final String REQUEST_ALL = "all";
  /**
   * 
   */
  public static final String REQUEST_ALL_BY_DATE = "allByDate";
  /**
   * 
   */
  public static final String REQUEST_DEFAULT = "default";
  /**
   * 
   */
  public static final String REQUEST_DEFAULT_BY_DATE = "defaultByDate";
  /**
   * 
   */
  public static final String REQUEST_UID = "uid";
  /**
   * 
   */
  public static final String REQUEST_UID_BY_DATE = "uidByDate";
  /**
   * 
   */
  public static final String REQUEST_MULTIPLE = "multiple";
  /**
   * 
   */
  public static final String REQUEST_IDENTIFIER_SET = "identifierSet";
  /**
   * 
   */
  public static final String REQUEST_DATA_SOURCE = "dataSource";
  /**
   * 
   */
  public static final String REQUEST_DATA_PROVIDER = "dataProvider";
  /**
   * 
   */
  public static final String REQUEST_DATA_FIELD = "dataField";
  /**
   * 
   */
  public static final String REQUEST_START = "start";
  /**
   * 
   */
  public static final String REQUEST_INCLUSIVE_START = "inclusiveStart";
  /**
   * 
   */
  public static final String REQUEST_END = "end";
  /**
   * 
   */
  public static final String REQUEST_EXCLUSIVE_END = "exclusiveEnd";

}
