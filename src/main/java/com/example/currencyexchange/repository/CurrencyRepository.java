package com.example.currencyexchange.repository;

import com.example.currencyexchange.model.entity.Currency;
import com.example.currencyexchange.util.DatabasePreparation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface CurrencyRepository {
    List<Currency> findAllCurrencies() throws SQLException;

    Optional<Currency> findCurrencyByCode(String code) throws SQLException;

    Long addCurrency(Currency currency) throws SQLException;
}
