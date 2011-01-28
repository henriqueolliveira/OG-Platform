/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import java.math.BigDecimal;

import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;

import org.junit.Test;

import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.CounterpartyImpl;
import com.opengamma.core.position.impl.TradeImpl;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Test the {@link TradeBuilder} class.
 */
public class TradeBuilderTest extends AbstractBuilderTestCase {

  @Test
  public void testEmpty() {
    TradeImpl trade = new TradeImpl();
    assertEncodeDecodeCycle(Trade.class, trade);
  }

  @Test
  public void testFull() {
    TradeImpl trade = new TradeImpl();
    trade.setUniqueId(UniqueIdentifier.of("A", "B"));
    trade.setParentPositionId(UniqueIdentifier.of("C", "D"));
    trade.setQuantity(BigDecimal.valueOf(12.34d));
    trade.setSecurityKey(IdentifierBundle.of(Identifier.of("E", "F")));
    trade.setCounterparty(new CounterpartyImpl(Identifier.of("G", "H")));
    trade.setTradeDate(LocalDate.of(2011, 1, 5));
    trade.setTradeTime(OffsetTime.parse("14:30+02:00"));
    assertEncodeDecodeCycle(Trade.class, trade);
  }

}