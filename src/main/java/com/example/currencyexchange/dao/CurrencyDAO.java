package com.example.currencyexchange.dao;

import com.example.currencyexchange.model.entity.Currency;
import com.example.currencyexchange.util.DatabasePreparation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CurrencyDAO {
    public List<Currency> findAllCurrencies() throws SQLException {
        String query = "SELECT * FROM currencies";

        List<Currency> currencies = new ArrayList<>();

        try (Connection connectingDataBase = DatabasePreparation.getConnection();
             PreparedStatement statement = connectingDataBase.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

             while (resultSet.next()) {
                 currencies.add(getCurrency(resultSet));
             }
        }
        return currencies;
    }

    public Optional<Currency> findCurrencyByCode(String code) throws SQLException {
        String query = "SELECT * FROM currencies WHERE code = ?";

        try (Connection connectingDataBase = DatabasePreparation.getConnection();
             PreparedStatement preparedStatement = connectingDataBase.prepareStatement(query)) {
             preparedStatement.setString(1, code);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(getCurrency(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    public Long addCurrency(Currency currency) throws SQLException {
        String query = "INSERT INTO currencies (code, full_name, sign) VALUES (?, ?, ?)";

        try (Connection connectingDataBase = DatabasePreparation.getConnection();
             PreparedStatement statement = connectingDataBase.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, currency.getCode());
            statement.setString(2, currency.getName());
            statement.setString(3, currency.getSign());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Creating currency failed, no ID obtained.");
                }
            }
        }
    }

    private static Currency getCurrency(ResultSet resultSet) throws SQLException {
        return new Currency(
                resultSet.getLong("id"),
                resultSet.getString("code"),
                resultSet.getString("full_name"),
                resultSet.getString("sign"));
    }
}