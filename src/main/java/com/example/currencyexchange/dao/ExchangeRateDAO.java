package com.example.currencyexchange.dao;

import com.example.currencyexchange.model.entity.Currency;
import com.example.currencyexchange.model.entity.ExchangeRate;
import com.example.currencyexchange.util.DatabasePreparation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateDAO {
    public List<ExchangeRate> findAllExchangeRates() throws SQLException{
        final String query =
                """
                SELECT
                    er.id AS exchange_rate_id,
                    bc.id AS base_id,
                    bc.code AS base_code,
                    bc.full_name AS base_full_name,
                    bc.sign AS base_sign,
                    tc.id AS target_id,
                    tc.code AS target_code,
                    tc.full_name AS target_full_name,
                    tc.sign AS target_sign,
                    er.rate AS rate
                FROM exchange_rates er
                JOIN currencies bc ON er.base_currency_id = bc.id
                JOIN currencies tc ON er.target_currency_id = tc.id
                """;

        try(Connection connectingDataBase = DatabasePreparation.getConnection();
            PreparedStatement statement = connectingDataBase.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery()) {

            List<ExchangeRate> exchangeRates = new ArrayList<>();
            while (resultSet.next()) {
                exchangeRates.add(mapRowToExchangeRate(resultSet));
            }
            return exchangeRates;
        }
    }

    public Optional<ExchangeRate> findExchangeRateByCodes(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        String query =
                """
                SELECT
                    er.id AS exchange_rate_id,
                    bc.id AS base_id,
                    bc.code AS base_code,
                    bc.full_name AS base_full_name,
                    bc.sign AS base_sign,
                    tc.id AS target_id,
                    tc.code AS target_code,
                    tc.full_name AS target_full_name,
                    tc.sign AS target_sign,
                    er.rate AS rate
                FROM exchange_rates er
                JOIN currencies bc ON er.base_currency_id = bc.id
                JOIN currencies tc ON er.target_currency_id = tc.id
                WHERE bc.code = ? AND tc.code = ?
                """;

        try(Connection connectingDataBase = DatabasePreparation.getConnection();
            PreparedStatement statement = connectingDataBase.prepareStatement(query)) {

            statement.setString(1, baseCurrencyCode);
            statement.setString(2, targetCurrencyCode);

            try(ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapRowToExchangeRate(resultSet));
            }
        }
    }

    public List<ExchangeRate> findUsdBaseRates(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        final String query =
                """
                SELECT
                    er.id AS exchange_rate_id,
                    bc.id AS base_id,
                    bc.code AS base_code,
                    bc.full_name AS base_full_name,
                    bc.sign AS base_sign,
                    tc.id AS target_id,
                    tc.code AS target_code,
                    tc.full_name AS target_full_name,
                    tc.sign AS target_sign,
                    er.rate AS rate
                FROM exchange_rates er
                JOIN currencies bc ON er.base_currency_id = bc.id
                JOIN currencies tc ON er.target_currency_id = tc.id
                WHERE (bc.code = 'USD' AND tc.code IN (?, ?))
                OR (tc.code = 'USD' AND bc.code IN (?, ?))
                """;

        try(Connection connectingDataBase = DatabasePreparation.getConnection();
            PreparedStatement statement = connectingDataBase.prepareStatement(query)) {

            statement.setString(1, baseCurrencyCode);
            statement.setString(2, targetCurrencyCode);
            statement.setString(3, baseCurrencyCode);
            statement.setString(4, targetCurrencyCode);

            ResultSet resultSet = statement.executeQuery();

            List<ExchangeRate> exchangeRatesList = new ArrayList<>();
            while (resultSet.next()) {
                exchangeRatesList.add(mapRowToExchangeRate(resultSet));
            }
            return exchangeRatesList;
        }
    }

    public Long addExchangeRate(ExchangeRate exchangeRate) throws SQLException {
        String query = "INSERT INTO exchange_rates (base_currency_id, target_currency_id, rate) VALUES (?, ?, ?)";

        try(Connection connection = DatabasePreparation.getConnection();
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            statement.setLong(1, exchangeRate.getBaseCurrency().getId());
            statement.setLong(2, exchangeRate.getTargetCurrency().getId());
            statement.setBigDecimal(3, exchangeRate.getRate());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Creating exchange rate failed, no ID obtained.");
                }
            }
        }
    }

    public void updateExchangeRate(ExchangeRate exchangeRate) throws SQLException {
         final String query =
                 """
                 UPDATE exchange_rates
                 SET base_currency_id = ?, target_currency_id = ?, rate = ?
                 WHERE id = ?
                 """;

         try (Connection connection = DatabasePreparation.getConnection();
              PreparedStatement statement = connection.prepareStatement(query)) {

             statement.setLong (1, exchangeRate.getBaseCurrency().getId());
             statement.setLong (2, exchangeRate.getTargetCurrency().getId());
             statement.setBigDecimal(3, exchangeRate.getRate());
             statement.setLong(4, exchangeRate.getId());

             statement.executeUpdate();
        }
    }

    private static ExchangeRate mapRowToExchangeRate(ResultSet resultSet) throws SQLException {
        return new ExchangeRate(
                resultSet.getLong("exchange_rate_id"),
                new Currency(
                        resultSet.getLong("base_id"),
                        resultSet.getString("base_code"),
                        resultSet.getString("base_full_name"),
                        resultSet.getString("base_sign")
                ),

                new Currency(
                        resultSet.getLong("target_id"),
                        resultSet.getString("target_code"),
                        resultSet.getString("target_full_name"),
                        resultSet.getString("target_sign")
                ),
                resultSet.getBigDecimal("rate")
        );
    }
}
