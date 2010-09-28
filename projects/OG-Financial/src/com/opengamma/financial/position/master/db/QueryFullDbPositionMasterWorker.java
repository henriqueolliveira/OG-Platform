/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.db;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Stack;

import javax.time.Instant;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import com.google.common.base.Objects;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionImpl;
import com.opengamma.financial.position.master.FullPortfolioGetRequest;
import com.opengamma.financial.position.master.FullPortfolioNodeGetRequest;
import com.opengamma.financial.position.master.FullPositionGetRequest;
import com.opengamma.financial.position.master.ManageablePosition;
import com.opengamma.financial.position.master.PositionSearchHistoricRequest;
import com.opengamma.financial.position.master.PositionSearchHistoricResult;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.tuple.LongObjectPair;

/**
 * Position master worker to get the fully populated portfolio elements.
 */
public class QueryFullDbPositionMasterWorker extends DbPositionMasterWorker {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(QueryFullDbPositionMasterWorker.class);

  /**
   * Creates an instance.
   */
  public QueryFullDbPositionMasterWorker() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  protected Portfolio getFullPortfolio(final FullPortfolioGetRequest request) {
    s_logger.debug("getFullPortfolio: {}", request);
    final Instant[] instants = defaultInstants(request.getPortfolioId());
    return selectFullPortfolio(request.getPortfolioId().toLatest(),
        Objects.firstNonNull(request.getVersionAsOfInstant(), instants[0]),
        Objects.firstNonNull(request.getCorrectedToInstant(), instants[1]));
  }

  //-------------------------------------------------------------------------
  @Override
  protected PortfolioNode getFullPortfolioNode(final FullPortfolioNodeGetRequest request) {
    s_logger.debug("getFullPortfolioNode: {}", request);
    final Instant[] instants = defaultInstants(request.getPortfolioNodeId());
    return selectFullPortfolioNode(request.getPortfolioNodeId().toLatest(),
        Objects.firstNonNull(request.getVersionAsOfInstant(), instants[0]),
        Objects.firstNonNull(request.getCorrectedToInstant(), instants[1]));
  }

  //-------------------------------------------------------------------------
  @Override
  protected Position getFullPosition(final FullPositionGetRequest request) {
    s_logger.debug("getFullPosition: {}", request);
    final Instant now = Instant.now(getTimeSource());
    final PositionSearchHistoricRequest searchRequest = new PositionSearchHistoricRequest(
        request.getPositionId(),
        Objects.firstNonNull(request.getVersionAsOfInstant(), now),
        Objects.firstNonNull(request.getCorrectedToInstant(), now));
    final PositionSearchHistoricResult searchResult = getMaster().searchPositionHistoric(searchRequest);
    final ManageablePosition firstPosition = searchResult.getFirstPosition();
    if (firstPosition == null || (request.getPositionId().isVersioned() && request.getPositionId().equals(firstPosition.getUniqueIdentifier()) == false)) {
      return null;
    }
    return new PositionImpl(firstPosition.getUniqueIdentifier(), firstPosition.getQuantity(), firstPosition.getSecurityKey());
  }

  //-------------------------------------------------------------------------
  protected Portfolio selectFullPortfolio(final UniqueIdentifier id, final Instant versionAsOf, final Instant correctedTo) {
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("portfolio_oid", extractOid(id))
      .addTimestamp("version_as_of", versionAsOf)
      .addTimestamp("corrected_to", correctedTo);
    final String sql = sqlSelectFullPortfolio(id.toLatest());
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final FullPortfolioDocumentExtractor extractor = new FullPortfolioDocumentExtractor();
    return (Portfolio) namedJdbc.query(sql, args, extractor);
  }

  /**
   * Gets the SQL for selecting a portfolio.
   * @param id  the identifier being searched for, not null
   * @return the SQL search, not null
   */
  protected String sqlSelectFullPortfolio(final UniqueIdentifier id) {
    String selectMax =
      "SELECT MAX(ver_from_instant) AS fixed_ver, MAX(corr_from_instant) AS fixed_corr " +
        "FROM pos_position " +
        "WHERE portfolio_oid = :portfolio_oid " +
          "AND ver_from_instant <= :version_as_of AND ver_to_instant > :version_as_of " +
          "AND corr_from_instant <= :corrected_to AND corr_to_instant > :corrected_to ";
    String sql =
      "SELECT " +
        "f.oid AS portfolio_oid, " +
        "f.ver_from_instant AS ver_from_instant, " +
        "f.corr_from_instant AS corr_from_instant, " +
        "f.name AS portfolio_name, " +
        "n.id AS node_id, " +
        "n.oid AS node_oid, " +
        "n.tree_left AS tree_left, " +
        "n.tree_right AS tree_right, " +
        "n.name AS node_name, " +
        "p.id AS position_id, " +
        "p.oid AS position_oid, " +
        "p.quantity AS quantity, " +
        "s.id_scheme AS seckey_scheme, " +
        "s.id_value AS seckey_value, " +
        "m.fixed_ver AS fixed_ver, " +
        "m.fixed_corr AS fixed_corr " +
      "FROM pos_portfolio f " +
        "LEFT JOIN pos_node n ON (n.portfolio_id = f.id) " +
        "LEFT JOIN pos_position p ON (p.parent_node_oid = n.oid " +
          "AND p.ver_from_instant <= :version_as_of AND p.ver_to_instant > :version_as_of " +
          "AND p.corr_from_instant <= :corrected_to AND p.corr_to_instant > :corrected_to) " +
        "LEFT JOIN pos_securitykey s ON (s.position_id = p.id), " +
        "(" + selectMax + ") m " +
      "WHERE f.oid = :portfolio_oid " +
        "AND f.ver_from_instant <= :version_as_of AND f.ver_to_instant > :version_as_of " +
        "AND f.corr_from_instant <= :corrected_to AND f.corr_to_instant > :corrected_to " +
      "ORDER BY n.tree_left, p.id ";
    return sql;
  }

  //-------------------------------------------------------------------------
  protected PortfolioNode selectFullPortfolioNode(final UniqueIdentifier id, final Instant versionAsOf, final Instant correctedTo) {
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("node_oid", extractOid(id))
      .addTimestamp("version_as_of", versionAsOf)
      .addTimestamp("corrected_to", correctedTo);
    final String sql = sqlSelectFullPortfolioNode(id.toLatest());
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final FullPortfolioDocumentExtractor extractor = new FullPortfolioDocumentExtractor();
    Portfolio portfolio = (Portfolio) namedJdbc.query(sql, args, extractor);
    return (portfolio != null ? portfolio.getRootNode() : null);
  }

  /**
   * Gets the SQL for selecting a portfolio.
   * @param id  the identifier being searched for, not null
   * @return the SQL search, not null
   */
  protected String sqlSelectFullPortfolioNode(final UniqueIdentifier id) {
    String selectMax =
      "SELECT MAX(ver_from_instant) AS fixed_ver, MAX(corr_from_instant) AS fixed_corr " +
        "FROM pos_position " +
        "WHERE portfolio_oid = (SELECT DISTINCT portfolio_oid FROM pos_node WHERE oid = :node_oid) " +
          "AND ver_from_instant <= :version_as_of AND ver_to_instant > :version_as_of " +
          "AND corr_from_instant <= :corrected_to AND corr_to_instant > :corrected_to ";
    String sql =
      "SELECT " +
        "f.oid AS portfolio_oid, " +
        "f.ver_from_instant AS ver_from_instant, " +
        "f.corr_from_instant AS corr_from_instant, " +
        "f.name AS portfolio_name, " +
        "n.id AS node_id, " +
        "n.oid AS node_oid, " +
        "n.tree_left AS tree_left, " +
        "n.tree_right AS tree_right, " +
        "n.name AS node_name, " +
        "p.id AS position_id, " +
        "p.oid AS position_oid, " +
        "p.quantity AS quantity, " +
        "s.id_scheme AS seckey_scheme, " +
        "s.id_value AS seckey_value, " +
        "m.fixed_ver AS fixed_ver, " +
        "m.fixed_corr AS fixed_corr " +
      "FROM " +
        "pos_portfolio f, " +
        "pos_node base, " +
        "pos_node n " +
          "LEFT JOIN pos_position p ON (p.parent_node_oid = n.oid " +
            "AND p.ver_from_instant <= :version_as_of AND p.ver_to_instant > :version_as_of " +
            "AND p.corr_from_instant <= :corrected_to AND p.corr_to_instant > :corrected_to) " +
          "LEFT JOIN pos_securitykey s ON (s.position_id = p.id), " +
          "(" + selectMax + ") m " +
      "WHERE base.portfolio_id = f.id " +
        "AND n.portfolio_id = f.id " +
        "AND base.oid = :node_oid " +
        "AND n.tree_left >= base.tree_left AND n.tree_right <= base.tree_right " +
        "AND f.ver_from_instant <= :version_as_of AND f.ver_to_instant > :version_as_of " +
        "AND f.corr_from_instant <= :corrected_to AND f.corr_to_instant > :corrected_to " +
      "ORDER BY n.tree_left, p.id ";
    return sql;
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a Portfolio.
   */
  protected final class FullPortfolioDocumentExtractor implements ResultSetExtractor {
    private final Stack<LongObjectPair<PortfolioNodeImpl>> _nodes = new Stack<LongObjectPair<PortfolioNodeImpl>>();

    @Override
    public Object extractData(ResultSet rs) throws SQLException, DataAccessException {
      String fixedInstants = null;
      PortfolioImpl portfolio = null;
      long lastNodeId = -1;
      PortfolioNodeImpl node = null;
      PositionImpl position = null;
      Long lastPositionId = Long.valueOf(-1);
      while (rs.next()) {
        if (portfolio == null) {
          final Timestamp maxPosVer = rs.getTimestamp("FIXED_VER");
          final Timestamp maxPosCorr = rs.getTimestamp("FIXED_CORR");
          final Timestamp maxPorVer = rs.getTimestamp("VER_FROM_INSTANT");
          final Timestamp maxPorCorr = rs.getTimestamp("CORR_FROM_INSTANT");
          final Instant maxVer = CompareUtils.max(DbDateUtils.fromSqlTimestamp(maxPosVer), DbDateUtils.fromSqlTimestamp(maxPorVer));
          final Instant maxCorr = CompareUtils.max(DbDateUtils.fromSqlTimestamp(maxPosCorr), DbDateUtils.fromSqlTimestamp(maxPorCorr));
          fixedInstants = createFixedInstants(maxVer, maxCorr);
          final long portfolioOid = rs.getLong("PORTFOLIO_OID");
          final String name = StringUtils.defaultString(rs.getString("PORTFOLIO_NAME"));
          final UniqueIdentifier uid = createUniqueIdentifier(portfolioOid, fixedInstants);
          portfolio = new PortfolioImpl(uid, name);
        }
        final long nodeId = rs.getLong("NODE_ID");
        if (nodeId != lastNodeId) {
          lastNodeId = nodeId;
          node = buildNode(rs, portfolio, fixedInstants);
        }
        final Long positionId = (Long) rs.getObject("POSITION_ID");
        if (positionId != null && positionId.equals(lastPositionId) == false) {
          lastPositionId = positionId;
          position = buildPosition(rs, node);
        }
        final String idScheme = rs.getString("SECKEY_SCHEME");
        final String idValue = rs.getString("SECKEY_VALUE");
        if (idScheme != null && idValue != null) {
          Identifier id = Identifier.of(idScheme, idValue);
          position.setSecurityKey(position.getSecurityKey().withIdentifier(id));
        }
      }
      return portfolio;
    }

    private PortfolioNodeImpl buildNode(ResultSet rs, final PortfolioImpl portfolio, final String fixedInstants) throws SQLException {
      final long nodeOid = rs.getLong("NODE_OID");
      final long treeLeft = rs.getLong("TREE_LEFT");
      final long treeRight = rs.getLong("TREE_RIGHT");
      final String name = StringUtils.defaultString(rs.getString("NODE_NAME"));
      final UniqueIdentifier uid = createUniqueIdentifier(nodeOid, fixedInstants);
      final PortfolioNodeImpl node = new PortfolioNodeImpl(uid, name);
      if (_nodes.size() == 0) {
        portfolio.setRootNode(node);
      } else {
        while (treeLeft > _nodes.peek().first) {
          _nodes.pop();
        }
        final PortfolioNodeImpl parent = _nodes.peek().second;
        parent.addChildNode(node);
      }
      _nodes.push(LongObjectPair.of(treeRight, node));
      return node;
    }

    private PositionImpl buildPosition(ResultSet rs, final PortfolioNodeImpl node) throws SQLException {
      final long positionId = rs.getLong("POSITION_ID");
      final long positionOid = rs.getLong("POSITION_OID");
      final BigDecimal quantity = extractBigDecimal(rs, "QUANTITY");
      final UniqueIdentifier uid = createUniqueIdentifier(positionOid, positionId, null);
      PositionImpl pos = new PositionImpl(uid, quantity, IdentifierBundle.EMPTY);
      node.addPosition(pos);
      return pos;
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Extracts the default instants to use in the search, from now or the version/correction uid.
   * @param uid  the identifier, not null
   * @return the version and correction instants, not null
   */
  protected Instant[] defaultInstants(final UniqueIdentifier uid) {
    final Instant now = Instant.now(getTimeSource());
    Instant version = now;
    Instant correction = now;
    final String[] splitVersion  = StringUtils.split(uid.getVersion(), '-');
    if (splitVersion != null && splitVersion.length == 2) {
      final long versionMillis = Long.parseLong(splitVersion[0], 16);
      version = Instant.ofEpochMillis(versionMillis);
      correction = Instant.ofEpochMillis(versionMillis + Long.parseLong(splitVersion[1], 16));
    }
    return new Instant[] {version, correction};
  }

  /**
   * Creates a string representing the version/correction.
   * @param maxVer  the maximum position version, may be null
   * @param maxCorr  the maximum position correction, may be null
   * @return the fixed instants version string, not null
   */
  protected String createFixedInstants(final Instant maxVer, final Instant maxCorr) {
    final long verMillis = maxVer.toEpochMillisLong();
    final long corrMillis = maxCorr.toEpochMillisLong();
    final long corrMillisDiff = corrMillis - verMillis;
    return Long.toHexString(verMillis) + "-" + Long.toHexString(corrMillisDiff);
  }

  /**
   * Creates a unique identifier for the full portfolio.
   * @param oid  the portfolio object identifier
   * @param fixedInstants  the fixed instants string
   * @return the unique identifier, not null
   */
  protected UniqueIdentifier createUniqueIdentifier(final long oid, final String fixedInstants) {
    return UniqueIdentifier.of(getIdentifierScheme(), Long.toString(oid), fixedInstants);
  }

}