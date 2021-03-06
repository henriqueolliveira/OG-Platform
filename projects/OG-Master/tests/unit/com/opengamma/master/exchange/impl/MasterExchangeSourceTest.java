/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange.impl;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.time.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.exchange.Exchange;
import com.opengamma.core.region.RegionUtils;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.master.exchange.ManageableExchange;
import com.opengamma.util.db.PagingRequest;

/**
 * Test MasterExchangeSource.
 */
@Test
public class MasterExchangeSourceTest {

  private static final UniqueIdentifier UID = UniqueIdentifier.of("A", "B");
  private static final Identifier ID = Identifier.of("C", "D");
  private static final IdentifierBundle BUNDLE = IdentifierBundle.of(ID);
  private static final Instant NOW = Instant.now();
  private static final VersionCorrection VC = VersionCorrection.of(NOW.minusSeconds(2), NOW.minusSeconds(1));

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_1arg_nullMaster() throws Exception {
    new MasterExchangeSource(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_2arg_nullMaster() throws Exception {
    new MasterExchangeSource(null, null);
  }

  //-------------------------------------------------------------------------
  public void test_getExchange_noOverride_found() throws Exception {
    ExchangeMaster mock = mock(ExchangeMaster.class);
    
    ExchangeDocument doc = new ExchangeDocument(example());
    when(mock.get(UID)).thenReturn(doc);
    MasterExchangeSource test = new MasterExchangeSource(mock);
    Exchange testResult = test.getExchange(UID);
    verify(mock, times(1)).get(UID);
    
    assertEquals(example(), testResult);
  }

  public void test_getExchange_found() throws Exception {
    ExchangeMaster mock = mock(ExchangeMaster.class);
    
    ExchangeDocument doc = new ExchangeDocument(example());
    when(mock.get(UID, VC)).thenReturn(doc);
    MasterExchangeSource test = new MasterExchangeSource(mock, VC);
    Exchange testResult = test.getExchange(UID);
    verify(mock, times(1)).get(UID, VC);
    
    assertEquals(example(), testResult);
  }

  public void test_getExchange_notFound() throws Exception {
    ExchangeMaster mock = mock(ExchangeMaster.class);
    
    when(mock.get(UID, VC)).thenThrow(new DataNotFoundException(""));
    MasterExchangeSource test = new MasterExchangeSource(mock, VC);
    Exchange testResult = test.getExchange(UID);
    verify(mock, times(1)).get(UID, VC);
    
    assertEquals(null, testResult);
  }

  //-------------------------------------------------------------------------
  public void test_getSingleExchange_Identifier_found() throws Exception {
    ExchangeMaster mock = mock(ExchangeMaster.class);
    ExchangeSearchRequest request = new ExchangeSearchRequest(ID);
    request.setPagingRequest(PagingRequest.ONE);
    request.setPagingRequest(PagingRequest.ONE);
    request.setVersionCorrection(VC);
    
    ExchangeSearchResult result = new ExchangeSearchResult();
    result.getDocuments().add(new ExchangeDocument(example()));
    
    when(mock.search(request)).thenReturn(result);
    MasterExchangeSource test = new MasterExchangeSource(mock, VC);
    Exchange testResult = test.getSingleExchange(ID);
    verify(mock, times(1)).search(request);
    
    assertEquals(example(), testResult);
  }

  public void test_getSingleExchange_Identifier_noFound() throws Exception {
    ExchangeMaster mock = mock(ExchangeMaster.class);
    ExchangeSearchRequest request = new ExchangeSearchRequest(ID);
    request.setPagingRequest(PagingRequest.ONE);
    request.setPagingRequest(PagingRequest.ONE);
    request.setVersionCorrection(VC);
    
    ExchangeSearchResult result = new ExchangeSearchResult();
    
    when(mock.search(request)).thenReturn(result);
    MasterExchangeSource test = new MasterExchangeSource(mock, VC);
    Exchange testResult = test.getSingleExchange(ID);
    verify(mock, times(1)).search(request);
    
    assertEquals(null, testResult);
  }

  //-------------------------------------------------------------------------
  public void test_getSingleExchange_IdentifierBundle_found() throws Exception {
    ExchangeMaster mock = mock(ExchangeMaster.class);
    ExchangeSearchRequest request = new ExchangeSearchRequest(BUNDLE);
    request.setPagingRequest(PagingRequest.ONE);
    request.setVersionCorrection(VC);
    
    ExchangeSearchResult result = new ExchangeSearchResult();
    result.getDocuments().add(new ExchangeDocument(example()));
    
    when(mock.search(request)).thenReturn(result);
    MasterExchangeSource test = new MasterExchangeSource(mock, VC);
    Exchange testResult = test.getSingleExchange(BUNDLE);
    verify(mock, times(1)).search(request);
    
    assertEquals(example(), testResult);
  }

  //-------------------------------------------------------------------------
  protected ManageableExchange example() {
    ManageableExchange exchange = new ManageableExchange();
    exchange.setUniqueId(UID);
    exchange.setName("NYSE");
    exchange.setRegionKey(IdentifierBundle.of(RegionUtils.countryRegionId("US")));
    return exchange;
  }

}
