package com.example.currencyexchange.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabasePreparation {
    private static final String DB_URL = "jdbc:sqlite:currency_exchange.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite JDBC driver not found.");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public void creatingTablesForDatabase() {
        final String createTableCurrencies =
                """
                CREATE TABLE IF NOT EXISTS currencies (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    code TEXT UNIQUE NOT NULL,
                    full_name TEXT NOT NULL,
                    sign TEXT NOT NULL
                );
                """;

        final String createTableExchangeRates =
                """
                CREATE TABLE IF NOT EXISTS exchange_rates (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    base_currency_id INTEGER NOT NULL,
                    target_currency_id INTEGER NOT NULL,
                    rate REAL NOT NULL,
                    UNIQUE(base_currency_id, target_currency_id),
                    FOREIGN KEY(base_currency_id) REFERENCES currencies(id),
                    FOREIGN KEY(target_currency_id) REFERENCES currencies(id)
                );
                """;
        try (Connection connectingDataBase = getConnection();
             Statement statement = connectingDataBase.createStatement()) {
             statement.execute(createTableCurrencies);
             statement.execute(createTableExchangeRates);

        } catch (SQLException e) {
            System.err.println("Ошибка инициализации: " + e.getMessage());
        }
    }
}
