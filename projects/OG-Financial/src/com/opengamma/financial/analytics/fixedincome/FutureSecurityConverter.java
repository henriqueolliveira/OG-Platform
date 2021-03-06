/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.exchange.ExchangeSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.instrument.FixedIncomeFutureInstrumentDefinition;
import com.opengamma.financial.instrument.future.IRFutureConvention;
import com.opengamma.financial.instrument.future.IRFutureDefinition;
import com.opengamma.financial.security.future.FutureSecurityVisitor;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class FutureSecurityConverter implements FutureSecurityVisitor<FixedIncomeFutureInstrumentDefinition<?>> {
  private final HolidaySource _holidaySource;
  private final ConventionBundleSource _conventionSource;
//  private final ExchangeSource _exchangeSource;

  public FutureSecurityConverter(final HolidaySource holidaySource, final ConventionBundleSource conventionSource,
      final ExchangeSource exchangeSource) {
    Validate.notNull(holidaySource, "holiday source");
    Validate.notNull(conventionSource, "convention source");
    Validate.notNull(exchangeSource, "exchange source");
    _holidaySource = holidaySource;
    _conventionSource = conventionSource;
//    _exchangeSource = exchangeSource;
  }

  @Override
  public FixedIncomeFutureInstrumentDefinition<?> visitAgricultureFutureSecurity(
      final AgricultureFutureSecurity security) {
    throw new OpenGammaRuntimeException(
        "Cannot convert AgricultureFutureSecurity to a FixedIncomeFutureInstrumentDefinition");
  }

  @Override
  public FixedIncomeFutureInstrumentDefinition<?> visitBondFutureSecurity(final BondFutureSecurity security) {
    throw new OpenGammaRuntimeException("Cannot convert BondFutureSecurity to a FixedIncomeFutureInstrumentDefinition");
    //  if (_underlyingConverter == null) {
    //  _underlyingConverter = new SecurityToFixedIncomeDefinitionConverter(_holidaySource, _conventionSource, _regionSource);
    //}
    //final LocalDate deliveryDateLD = null;//deliveryDate.toLocalDate();
    //final List<BondFutureDeliverable> deliverableBasket = security.getBasket();
    //final int n = deliverableBasket.size();
    //final BondDefinition[] deliverableBonds = new BondDefinition[n];
    //final double[] conversionFactors = new double[n];
    //final int i = 0;
    //for (final BondFutureDeliverable bfd : deliverableBasket) {
    //  final BondSecurity underlyingBond = null;
    //  final LocalDate lastTradeDate = underlyingBond.getLastTradeDate().getExpiry().toLocalDate();
    //  Validate.isTrue(deliveryDateLD.isBefore(lastTradeDate), "The bond has expired before delivery");
    //  //TODO bond futures are exchange-traded - check that this is the same calendar for the exchange as the currency
    //  final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, security.getCurrency());
    //  final CurrencyUnit currency = security.getCurrency();
    //  final String conventionName = currency + "_BOND_FUTURE_DELIVERABLE_CONVENTION";
    //  final Identifier id = Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, conventionName);
    //  final ConventionBundle conventionBundle = _conventionSource.getConventionBundle(id);
    //  Validate.notNull(conventionBundle, "convention bundle " + conventionName);
    //  final BusinessDayConvention businessDayConvention = conventionBundle.getBusinessDayConvention();
    //  final BondDefinition deliverable = (BondDefinition) _underlyingConverter.visitBondSecurity(underlyingBond, conventionBundle);
    //  final BondConvention bondForwardConvention = new BondConvention(conventionBundle.getSettlementDays(), conventionBundle.getDayCount(), businessDayConvention, calendar,
    //      conventionBundle.isEOMConvention(), conventionName, conventionBundle.getExDividendDays(), conventionBundle.getYieldConvention());
    //
    //}
    //return null;//new BondForwardDefinition(underlyingBond, deliveryDate.toLocalDate(), bondForwardConvention);
  }

  @Override
  public FixedIncomeFutureInstrumentDefinition<?> visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
    throw new OpenGammaRuntimeException(
        "Cannot convert EnergyFutureSecurity to a FixedIncomeFutureInstrumentDefinition");
  }

  @Override
  public FixedIncomeFutureInstrumentDefinition<?> visitFXFutureSecurity(final FXFutureSecurity security) {
    throw new OpenGammaRuntimeException("Cannot convert FXFutureSecurity to a FixedIncomeFutureInstrumentDefinition");
  }

  @Override
  public FixedIncomeFutureInstrumentDefinition<?> visitIndexFutureSecurity(final IndexFutureSecurity security) {
    throw new OpenGammaRuntimeException("Cannot convert IndexFutureSecurity to a FixedIncomeFutureInstrumentDefinition");
  }

  @Override
  public FixedIncomeFutureInstrumentDefinition<?> visitInterestRateFutureSecurity(
      final InterestRateFutureSecurity security) {
    final Currency currency = security.getCurrency();
    final ConventionBundle conventions = _conventionSource.getConventionBundle(Identifier.of(
        InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currency + "_IRFUTURE"));
    final Calendar calendar = CalendarUtil.getCalendar(_holidaySource, currency); //TODO use exchange holiday
    final BusinessDayConvention businessDayConvention = conventions.getBusinessDayConvention();
    // Expiry is actually the last trade date rather than the expiry date of the future so we have to go forward in time.
    final ZonedDateTime lastTradeDate = businessDayConvention.adjustDate(calendar, security.getExpiry().getExpiry());
    final ZonedDateTime settlementDate = businessDayConvention.adjustDate(calendar,
        lastTradeDate.plusDays(conventions.getSettlementDays()));
    final ZonedDateTime maturityDate = businessDayConvention.adjustDate(calendar, settlementDate.plusMonths(3));
    final IRFutureConvention convention = new IRFutureConvention(conventions.getSettlementDays(),
        conventions.getDayCount(), businessDayConvention, calendar, conventions.getFutureYearFraction(),
        currency.getCode() + "_IR_FUTURE_CONVENTION");
    return new IRFutureDefinition(lastTradeDate, maturityDate, convention);
  }

  @Override
  public FixedIncomeFutureInstrumentDefinition<?> visitMetalFutureSecurity(final MetalFutureSecurity security) {
    throw new OpenGammaRuntimeException("Cannot convert MetalFutureSecurity to a FixedIncomeFutureInstrumentDefinition");
  }

  @Override
  public FixedIncomeFutureInstrumentDefinition<?> visitStockFutureSecurity(final StockFutureSecurity security) {
    throw new OpenGammaRuntimeException("Cannot convert StockFutureSecurity to a FixedIncomeFutureInstrumentDefinition");
  }

}
