package com.example.currencyexchange.repository;

import com.example.currencyexchange.model.entity.ExchangeRate;

import java.sql.*;
import java.util.List;
import java.util.Optional;

public interface ExchangeRateRepository {
    List<ExchangeRate> findAllExchangeRates() throws SQLException;

    Optional<ExchangeRate> findExchangeRateByCodes(String baseCurrencyCode, String targetCurrencyCode) throws SQLException;

    List<ExchangeRate> findUsdBaseRates(String baseCurrencyCode, String targetCurrencyCode) throws SQLException;

    Long addExchangeRate(ExchangeRate exchangeRate) throws SQLException;

    void updateExchangeRate(ExchangeRate exchangeRate) throws SQLException;
}
