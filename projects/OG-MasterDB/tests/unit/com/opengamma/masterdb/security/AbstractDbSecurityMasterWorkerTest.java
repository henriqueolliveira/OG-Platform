/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import static com.opengamma.util.db.DbDateUtils.MAX_SQL_TIMESTAMP;
import static com.opengamma.util.db.DbDateUtils.toSqlTimestamp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.TimeZone;

import javax.time.Instant;
import javax.time.TimeSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.test.DBTest;

/**
 * Base tests for DbSecurityMasterWorker via DbSecurityMaster.
 */
@Ignore
public abstract class AbstractDbSecurityMasterWorkerTest extends DBTest {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDbSecurityMasterWorkerTest.class);

  protected DbSecurityMaster _secMaster;
  protected Instant _version1Instant;
  protected Instant _version2Instant;
  protected int _totalSecurities;

  public AbstractDbSecurityMasterWorkerTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _secMaster = (DbSecurityMaster) context.getBean(getDatabaseType() + "DbSecurityMaster");
    
//    id bigint not null,
//    oid bigint not null,
//    ver_from_instant timestamp not null,
//    ver_to_instant timestamp not null,
//    corr_from_instant timestamp not null,
//    corr_to_instant timestamp not null,
//    name varchar(255) not null,
//    sec_type varchar(255) not null,
    Instant now = Instant.now();
    _secMaster.setTimeSource(TimeSource.fixed(now));
    _version1Instant = now.minusSeconds(100);
    _version2Instant = now.minusSeconds(50);
    s_logger.debug("test data now:   {}", _version1Instant);
    s_logger.debug("test data later: {}", _version2Instant);
    final SimpleJdbcTemplate template = _secMaster.getDbSource().getJdbcTemplate();
    template.update("INSERT INTO sec_security VALUES (?,?,?,?,?, ?,?,?)",
        101, 101, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, "TestSecurity101", "EQUITY");
    template.update("INSERT INTO sec_security VALUES (?,?,?,?,?, ?,?,?)",
        102, 102, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, "TestSecurity102", "EQUITY");
    template.update("INSERT INTO sec_security VALUES (?,?,?,?,?, ?,?,?)",
        201, 201, toSqlTimestamp(_version1Instant), toSqlTimestamp(_version2Instant), toSqlTimestamp(_version1Instant), MAX_SQL_TIMESTAMP, "TestSecurity201", "EQUITY");
    template.update("INSERT INTO sec_security VALUES (?,?,?,?,?, ?,?,?)",
        202, 201, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, "TestSecurity202", "EQUITY");
    _totalSecurities = 3;
//  id bigint not null,
//  key_scheme varchar(255) not null,
//  key_value varchar(255) not null,
    template.update("INSERT INTO sec_idkey VALUES (?,?,?)",
        1, "A", "B");
    template.update("INSERT INTO sec_idkey VALUES (?,?,?)",
        2, "C", "D");
    template.update("INSERT INTO sec_idkey VALUES (?,?,?)",
        3, "E", "F");
    template.update("INSERT INTO sec_idkey VALUES (?,?,?)",
        4, "G", "H");
//  security_id bigint not null,
//  idkey_id bigint not null,
    template.update("INSERT INTO sec_security2idkey VALUES (?,?)",
        101, 1);
    template.update("INSERT INTO sec_security2idkey VALUES (?,?)",
        101, 2);
    template.update("INSERT INTO sec_security2idkey VALUES (?,?)",
        101, 3);
    template.update("INSERT INTO sec_security2idkey VALUES (?,?)",
        102, 1);
    template.update("INSERT INTO sec_security2idkey VALUES (?,?)",
        102, 2);
    template.update("INSERT INTO sec_security2idkey VALUES (?,?)",
        102, 4);
    template.update("INSERT INTO sec_security2idkey VALUES (?,?)",
        201, 2);
    template.update("INSERT INTO sec_security2idkey VALUES (?,?)",
        201, 3);
    template.update("INSERT INTO sec_security2idkey VALUES (?,?)",
        202, 2);
    template.update("INSERT INTO sec_security2idkey VALUES (?,?)",
        202, 3);
  }

  @After
  public void tearDown() throws Exception {
    _secMaster = null;
    super.tearDown();
  }

  //-------------------------------------------------------------------------
  protected void assert101(final SecurityDocument test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "101", "0");
    assertNotNull(test);
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableSecurity security = test.getSecurity();
    assertNotNull(security);
    assertEquals(uid, security.getUniqueIdentifier());
    assertEquals("TestSecurity101", security.getName());
    assertEquals("EQUITY", security.getSecurityType());
    assertEquals(IdentifierBundle.of(Identifier.of("A", "B"), Identifier.of("C", "D"), Identifier.of("E", "F")), security.getIdentifiers());
  }

  protected void assert102(final SecurityDocument test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "102", "0");
    assertNotNull(test);
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableSecurity security = test.getSecurity();
    assertNotNull(security);
    assertEquals(uid, security.getUniqueIdentifier());
    assertEquals("TestSecurity102", security.getName());
    assertEquals("EQUITY", security.getSecurityType());
    assertEquals(IdentifierBundle.of(Identifier.of("A", "B"), Identifier.of("C", "D"), Identifier.of("G", "H")), security.getIdentifiers());
  }

  protected void assert201(final SecurityDocument test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "201", "0");
    assertNotNull(test);
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version1Instant, test.getVersionFromInstant());
    assertEquals(_version2Instant, test.getVersionToInstant());
    assertEquals(_version1Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableSecurity security = test.getSecurity();
    assertNotNull(security);
    assertEquals(uid, security.getUniqueIdentifier());
    assertEquals("TestSecurity201", security.getName());
    assertEquals("EQUITY", security.getSecurityType());
    assertEquals(IdentifierBundle.of(Identifier.of("C", "D"), Identifier.of("E", "F")), security.getIdentifiers());
  }

  protected void assert202(final SecurityDocument test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbSec", "201", "1");
    assertNotNull(test);
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    ManageableSecurity security = test.getSecurity();
    assertNotNull(security);
    assertEquals(uid, security.getUniqueIdentifier());
    assertEquals("TestSecurity202", security.getName());
    assertEquals("EQUITY", security.getSecurityType());
    assertEquals(IdentifierBundle.of(Identifier.of("C", "D"), Identifier.of("E", "F")), security.getIdentifiers());
  }

}