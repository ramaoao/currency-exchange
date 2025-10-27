package com.example.currencyexchange.service.currency;

import com.example.currencyexchange.dao.CurrencyDAO;
import com.example.currencyexchange.model.errors.CurrencyNotFoundException;
import com.example.currencyexchange.model.entity.Currency;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CurrencyService {
    private final CurrencyDAO currencyDAO = new CurrencyDAO();

    public List<Currency> findAllCurrencies() throws SQLException {
        return currencyDAO.findAllCurrencies();
    }

    public Currency findCurrencyByCode(String code) throws SQLException {
        Optional<Currency> currency = currencyDAO.findCurrencyByCode(code.toUpperCase());

        if (currency.isEmpty()) {
            throw new CurrencyNotFoundException("Currency not found with code: " + code);
        }
        return currency.get();
    }

    public Currency addCurrency(String code, String name, String sign) throws SQLException {
        code = code.trim().toUpperCase();
        Currency currency = new Currency(code, name, sign);
        Optional<Currency> foundCode = currencyDAO.findCurrencyByCode(code);

        if (foundCode.isPresent()) {
            throw new CurrencyNotFoundException("Currency with code " + " already exists");
        }

        Long id = currencyDAO.addCurrency(currency);
        currency.setId(id);
        return currency;
    }
}
