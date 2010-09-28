/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.TimeZone;

import javax.time.Instant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.position.master.ManageablePosition;
import com.opengamma.financial.position.master.PositionDocument;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Tests ModifyPositionDbPositionMasterWorker.
 */
public class ModifyPositionDbPositionMasterWorkerAddPositionTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(ModifyPositionDbPositionMasterWorkerAddPositionTest.class);

  private ModifyPositionDbPositionMasterWorker _worker;
  private DbPositionMasterWorker _queryWorker;

  public ModifyPositionDbPositionMasterWorkerAddPositionTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new ModifyPositionDbPositionMasterWorker();
    _worker.init(_posMaster);
    _queryWorker = new QueryPositionDbPositionMasterWorker();
    _queryWorker.init(_posMaster);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _worker = null;
    _queryWorker = null;
  }

  //-------------------------------------------------------------------------
  @Test(expected = NullPointerException.class)
  public void test_addPosition_nullDocument() {
    _worker.addPosition(null);
  }

  @Test(expected = NullPointerException.class)
  public void test_addPosition_noParentNodeId() {
    ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    PositionDocument doc = new PositionDocument();
    doc.setPosition(position);
    _worker.addPosition(doc);
  }

  @Test(expected = NullPointerException.class)
  public void test_addPosition_noPosition() {
    PositionDocument doc = new PositionDocument();
    doc.setParentNodeId(UniqueIdentifier.of("DbPos", "111"));
    _worker.addPosition(doc);
  }

  @Test
  public void test_addPosition_add() {
    Instant now = Instant.now(_posMaster.getTimeSource());
    
    ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    PositionDocument doc = new PositionDocument();
    doc.setParentNodeId(UniqueIdentifier.of("DbPos", "111"));
    doc.setPosition(position);
    PositionDocument test = _worker.addPosition(doc);
    
    UniqueIdentifier uid = test.getPositionId();
    assertNotNull(uid);
    assertEquals("DbPos", uid.getScheme());
    assertTrue(uid.isVersioned());
    assertTrue(Long.parseLong(uid.getValue()) > 1000);
    assertEquals("0", uid.getVersion());
    assertEquals(UniqueIdentifier.of("DbPos", "101"), test.getPortfolioId());
    assertEquals(UniqueIdentifier.of("DbPos", "111"), test.getParentNodeId());
    assertEquals(now, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(now, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageablePosition testPosition = test.getPosition();
    assertNotNull(testPosition);
    assertEquals(uid, testPosition.getUniqueIdentifier());
    assertEquals(BigDecimal.TEN, testPosition.getQuantity());
    IdentifierBundle secKey = testPosition.getSecurityKey();
    assertNotNull(secKey);
    assertEquals(1, secKey.size());
    assertTrue(secKey.getIdentifiers().contains(Identifier.of("A", "B")));
  }

  @Test
  public void test_addPosition_addThenGet() {
    ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    PositionDocument doc = new PositionDocument();
    doc.setParentNodeId(UniqueIdentifier.of("DbPos", "111"));
    doc.setPosition(position);
    PositionDocument added = _worker.addPosition(doc);
    
    PositionDocument test = _queryWorker.getPosition(added.getPositionId());
    assertEquals(added, test);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPos]", _worker.toString());
  }

}