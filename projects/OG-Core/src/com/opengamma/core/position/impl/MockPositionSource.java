/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.PositionSource;
import com.opengamma.id.UniqueIdentifiables;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSupplier;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple mutable implementation of a source of positions.
 * <p>
 * This class is intended for testing scenarios.
 * It is not thread-safe and must not be used in production.
 */
public class MockPositionSource implements PositionSource {
  // this is currently public for indirect use by another project via ViewTestUtils

  /**
   * The portfolios.
   */
  private final Map<UniqueIdentifier, Portfolio> _portfolios = new ConcurrentHashMap<UniqueIdentifier, Portfolio>();
  /**
   * A cache of nodes by identifier.
   */
  private final Map<UniqueIdentifier, PortfolioNode> _nodes = new ConcurrentHashMap<UniqueIdentifier, PortfolioNode>();
  /**
   * A cache of positions by identifier.
   */
  private final Map<UniqueIdentifier, Position> _positions = new ConcurrentHashMap<UniqueIdentifier, Position>();
  /**
   * A cache of trades by identifier.
   */
  private final Map<UniqueIdentifier, Trade> _trades = new ConcurrentHashMap<UniqueIdentifier, Trade>();
  /**
   * The suppler of unique identifiers.
   */
  private final UniqueIdentifierSupplier _uidSupplier;

  /**
   * Creates an instance using the default scheme for each {@link UniqueIdentifier} created.
   */
  public MockPositionSource() {
    _uidSupplier = new UniqueIdentifierSupplier("Mock");
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the list of all portfolio identifiers.
   * 
   * @return the portfolio identifiers, unmodifiable, not null
   */
  public Set<UniqueIdentifier> getPortfolioIds() {
    return _portfolios.keySet();
  }

  /**
   * Gets a specific root portfolio by name.
   * 
   * @param identifier  the identifier, null returns null
   * @return the portfolio, null if not found
   */
  public Portfolio getPortfolio(UniqueIdentifier identifier) {
    return identifier == null ? null : _portfolios.get(identifier);
  }

  /**
   * Finds a specific node from any portfolio by identifier.
   * 
   * @param identifier  the identifier, null returns null
   * @return the node, null if not found
   */
  public PortfolioNode getPortfolioNode(UniqueIdentifier identifier) {
    return identifier == null ? null : _nodes.get(identifier);
  }

  /**
   * Finds a specific position from any portfolio by identifier.
   * 
   * @param identifier  the identifier, null returns null
   * @return the position, null if not found
   */
  public Position getPosition(UniqueIdentifier identifier) {
    return identifier == null ? null : _positions.get(identifier);
  }

  /**
   * Finds a specific trade from any portfolio by identifier.
   * 
   * @param uid  the identifier, null returns null
   * @return the trade, null if not found
   */
  @Override
  public Trade getTrade(UniqueIdentifier uid) {
    return uid == null ? null : _trades.get(uid);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a portfolio to the master.
   * 
   * @param portfolio  the portfolio to add, not null
   */
  public void addPortfolio(Portfolio portfolio) {
    ArgumentChecker.notNull(portfolio, "portfolio");

    _portfolios.put(portfolio.getUniqueId(), portfolio);
    addToCache(portfolio.getUniqueId().getValue(), null, portfolio.getRootNode());
  }

  /**
   * Adds a node to the cache.
   * 
   * @param portfolioId  the id, not null
   * @param node  the node to add, not null
   */
  private void addToCache(String portfolioId, UniqueIdentifier parentNode, PortfolioNode node) {
    // node
    if (node instanceof PortfolioNodeImpl) {
      PortfolioNodeImpl nodeImpl = (PortfolioNodeImpl) node;
      nodeImpl.setUniqueId(_uidSupplier.getWithValuePrefix(portfolioId + "-"));
      nodeImpl.setParentNodeId(parentNode);
    }
    _nodes.put(node.getUniqueId(), node);
    
    // position
    for (Position position : node.getPositions()) {
      if (position instanceof PositionImpl) {
        PositionImpl positionImpl = (PositionImpl) position;
        positionImpl.setUniqueId(_uidSupplier.getWithValuePrefix(portfolioId + "-"));
        positionImpl.setParentNodeId(node.getUniqueId());
        
        //add trades
        for (Trade trade : positionImpl.getTrades()) {
          UniqueIdentifiables.setInto(trade, _uidSupplier.getWithValuePrefix(portfolioId + "-"));
          _trades.put(trade.getUniqueId(), trade);
        }
      }
      _positions.put(position.getUniqueId(), position);
    }
    
    // recurse
    for (PortfolioNode child : node.getChildNodes()) {
      addToCache(portfolioId, node.getUniqueId(), child);
    }
  }

  /**
   * Removes a portfolio from the master.
   * 
   * @param portfolio  the portfolio to remove, not null
   */
  public void removePortfolio(Portfolio portfolio) {
    ArgumentChecker.notNull(portfolio, "portfolio");
    _portfolios.remove(portfolio.getUniqueId());
    removeFromCache(portfolio.getRootNode());
  }

  /**
   * Removes a node from the cache
   * 
   * @param node  the node to remove, not null
   */
  private void removeFromCache(PortfolioNode node) {
    _nodes.remove(node.getUniqueId());
    for (Position position : node.getPositions()) {
      for (Trade trade : position.getTrades()) {
        _trades.remove(trade.getUniqueId());
      }
      _positions.remove(position.getUniqueId());
    }
    for (PortfolioNode child : node.getChildNodes()) {
      removeFromCache(child);
    }
  }

}
