/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.config.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.config.ConfigDocument;
import com.opengamma.config.ConfigHistoryRequest;
import com.opengamma.config.ConfigHistoryResult;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.PagingRequest;

/**
 * Tests QueryConfigDbConfigMasterWorker.
 */
public class QueryConfigDbConfigTypeMasterWorkerHistoryTest extends AbstractDbConfigTypeMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryConfigDbConfigTypeMasterWorkerHistoryTest.class);

  private DbConfigTypeMasterWorker<Identifier> _worker;

  public QueryConfigDbConfigTypeMasterWorkerHistoryTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new QueryConfigDbConfigTypeMasterWorker<Identifier>();
    _worker.init(_cfgMaster);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _worker = null;
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_documents() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbCfg", "201");
    ConfigHistoryRequest request = new ConfigHistoryRequest(oid);
    ConfigHistoryResult<Identifier> test = _worker.history(request);
    
    assertEquals(2, test.getDocuments().size());
    ConfigDocument<Identifier> doc0 = test.getDocuments().get(0);  // new version
    ConfigDocument<Identifier> doc1 = test.getDocuments().get(1);  // old version
    
    assertEquals(UniqueIdentifier.of("DbCfg", "201", "1"), doc0.getConfigId());
    assertNotNull(doc0.getVersionFromInstant());
    assertEquals(null, doc0.getVersionToInstant());
    assertEquals(Identifier.of("A", "B"), doc0.getValue());
    assertEquals("TestConfig202", doc0.getName());
    
    assertEquals(UniqueIdentifier.of("DbCfg", "201", "0"), doc1.getConfigId());
    assertNotNull(doc1.getVersionFromInstant());
    assertEquals(doc0.getVersionFromInstant(), doc1.getVersionToInstant());
    assertEquals(Identifier.of("A", "B"), doc0.getValue());
    assertEquals("TestConfig201", doc1.getName());
  }

  @Test
  public void test_history_documentCountWhenMultipleSecurities() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbCfg", "102");
    ConfigHistoryRequest request = new ConfigHistoryRequest(oid);
    ConfigHistoryResult<Identifier> test = _worker.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    ConfigDocument<Identifier> doc0 = test.getDocuments().get(0);  // new version
    
    assertEquals(UniqueIdentifier.of("DbCfg", "102", "0"), doc0.getConfigId());
    assertNotNull(doc0.getVersionFromInstant());
    assertEquals(null, doc0.getVersionToInstant());
    assertEquals(Identifier.of("A", "B"), doc0.getValue());
    assertEquals("TestConfig102", doc0.getName());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbCfg", "201");
    ConfigHistoryRequest request = new ConfigHistoryRequest(oid);
    ConfigHistoryResult<Identifier> test = _worker.history(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    ConfigDocument<Identifier> doc0 = test.getDocuments().get(0);
    ConfigDocument<Identifier> doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbCfg", "201", "1"), doc0.getConfigId());
    assertEquals(UniqueIdentifier.of("DbCfg", "201", "0"), doc1.getConfigId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_noInstants_pageOne() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbCfg", "201");
    ConfigHistoryRequest request = new ConfigHistoryRequest(oid);
    request.setPagingRequest(new PagingRequest(1, 1));
    ConfigHistoryResult<Identifier> test = _worker.history(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    ConfigDocument<Identifier> doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbCfg", "201", "1"), doc0.getConfigId());
  }

  @Test
  public void test_history_noInstants_pageTwo() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbCfg", "201");
    ConfigHistoryRequest request = new ConfigHistoryRequest(oid);
    request.setPagingRequest(new PagingRequest(2, 1));
    ConfigHistoryResult<Identifier> test = _worker.history(request);
    
    assertNotNull(test);
    assertNotNull(test.getPaging());
    assertEquals(2, test.getPaging().getFirstItem());
    assertEquals(1, test.getPaging().getPagingSize());
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertNotNull(test.getDocuments());
    assertEquals(1, test.getDocuments().size());
    ConfigDocument<Identifier> doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbCfg", "201", "0"), doc0.getConfigId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_versionsFrom_preFirst() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbCfg", "201");
    ConfigHistoryRequest request = new ConfigHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.minusSeconds(5));
    ConfigHistoryResult<Identifier> test = _worker.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    ConfigDocument<Identifier> doc0 = test.getDocuments().get(0);
    ConfigDocument<Identifier> doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbCfg", "201", "1"), doc0.getConfigId());
    assertEquals(UniqueIdentifier.of("DbCfg", "201", "0"), doc1.getConfigId());
  }

  @Test
  public void test_history_versionsFrom_firstToSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbCfg", "201");
    ConfigHistoryRequest request = new ConfigHistoryRequest(oid);
    request.setVersionsFromInstant(_version1Instant.plusSeconds(5));
    ConfigHistoryResult<Identifier> test = _worker.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    ConfigDocument<Identifier> doc0 = test.getDocuments().get(0);
    ConfigDocument<Identifier> doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbCfg", "201", "1"), doc0.getConfigId());
    assertEquals(UniqueIdentifier.of("DbCfg", "201", "0"), doc1.getConfigId());
  }

  @Test
  public void test_history_versionsFrom_postSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbCfg", "201");
    ConfigHistoryRequest request = new ConfigHistoryRequest(oid);
    request.setVersionsFromInstant(_version2Instant.plusSeconds(5));
    ConfigHistoryResult<Identifier> test = _worker.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    ConfigDocument<Identifier> doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbCfg", "201", "1"), doc0.getConfigId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_history_versionsTo_preFirst() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbCfg", "201");
    ConfigHistoryRequest request = new ConfigHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.minusSeconds(5));
    ConfigHistoryResult<Identifier> test = _worker.history(request);
    
    assertEquals(0, test.getPaging().getTotalItems());
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_history_versionsTo_firstToSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbCfg", "201");
    ConfigHistoryRequest request = new ConfigHistoryRequest(oid);
    request.setVersionsToInstant(_version1Instant.plusSeconds(5));
    ConfigHistoryResult<Identifier> test = _worker.history(request);
    
    assertEquals(1, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    ConfigDocument<Identifier> doc0 = test.getDocuments().get(0);
    assertEquals(UniqueIdentifier.of("DbCfg", "201", "0"), doc0.getConfigId());
  }

  @Test
  public void test_history_versionsTo_postSecond() {
    UniqueIdentifier oid = UniqueIdentifier.of("DbCfg", "201");
    ConfigHistoryRequest request = new ConfigHistoryRequest(oid);
    request.setVersionsToInstant(_version2Instant.plusSeconds(5));
    ConfigHistoryResult<Identifier> test = _worker.history(request);
    
    assertEquals(2, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    ConfigDocument<Identifier> doc0 = test.getDocuments().get(0);
    ConfigDocument<Identifier> doc1 = test.getDocuments().get(1);
    assertEquals(UniqueIdentifier.of("DbCfg", "201", "1"), doc0.getConfigId());
    assertEquals(UniqueIdentifier.of("DbCfg", "201", "0"), doc1.getConfigId());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbCfg]", _worker.toString());
  }

}