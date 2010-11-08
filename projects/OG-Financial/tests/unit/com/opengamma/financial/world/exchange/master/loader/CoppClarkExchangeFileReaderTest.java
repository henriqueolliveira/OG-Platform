/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.exchange.master.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.opengamma.financial.world.exchange.ExchangeUtils;
import com.opengamma.financial.world.exchange.master.ExchangeDocument;
import com.opengamma.financial.world.exchange.master.ExchangeMaster;
import com.opengamma.financial.world.exchange.master.ExchangeSearchRequest;
import com.opengamma.financial.world.exchange.master.ExchangeSearchResult;
import com.opengamma.financial.world.exchange.master.loader.CoppClarkExchangeFileReader;
import com.opengamma.financial.world.region.RegionUtils;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;

/**
 * Test InMemoryExchangeMaster.
 */
public class CoppClarkExchangeFileReaderTest {

  private static String NAME = "Euronext LIFFE (UK contracts)";
  private static Identifier ID_LIFFE_MIC = ExchangeUtils.isoMicExchangeId("XLIF");

  private ExchangeMaster master;

  @Before
  public void setUp() {
    CoppClarkExchangeFileReader reader = CoppClarkExchangeFileReader.createPopulated();
    master = reader.getExchangeMaster();
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_liffe() {
    ExchangeSearchRequest request = new ExchangeSearchRequest(ID_LIFFE_MIC);
    ExchangeSearchResult result = master.search(request);
    assertEquals(1, result.getDocuments().size());
    ExchangeDocument doc = result.getFirstDocument();
    assertEquals("MemExg", doc.getExchangeId().getScheme());
    assertNotNull(doc.getVersionFromInstant());
    assertNotNull(doc.getCorrectionFromInstant());
    assertEquals(doc.getExchangeId(), doc.getExchange().getUniqueIdentifier());
    assertEquals(NAME, doc.getExchange().getName());
    assertEquals(IdentifierBundle.of(RegionUtils.countryRegionId("GB")), doc.getExchange().getRegionId());
    assertEquals(IdentifierBundle.of(ID_LIFFE_MIC), doc.getExchange().getIdentifiers());
  }

}