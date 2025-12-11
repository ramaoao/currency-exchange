package com.example.currencyexchange.service.exchange;

import com.example.currencyexchange.dao.ExchangeRateDAO;
import com.example.currencyexchange.model.entity.Currency;
import com.example.currencyexchange.model.entity.ExchangeRate;
import com.example.currencyexchange.model.errors.ExchangeRateAlreadyExistsException;
import com.example.currencyexchange.model.errors.ExchangeRateNotFoundException;
import com.example.currencyexchange.service.currency.CurrencyService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ExchangeRateService {
    private final ExchangeRateDAO exchangeRateDAO;
    private final CurrencyService currencyService;

    public ExchangeRateService(ExchangeRateDAO exchangeRateDAO, CurrencyService currencyService) {
        this.exchangeRateDAO = exchangeRateDAO;
        this.currencyService = currencyService;
    }

    public List<ExchangeRate> findAllExchangeRates() throws SQLException {
        return exchangeRateDAO.findAllExchangeRates();
    }

    public ExchangeRate findExchangeRateByCodes(String baseCode, String targetCode) throws SQLException {
        baseCode = baseCode.toUpperCase();
        targetCode = targetCode.toUpperCase();

        Optional<ExchangeRate> exchangeRate = exchangeRateDAO.findExchangeRateByCodes(baseCode, targetCode);
        if (exchangeRate.isEmpty()) {
            throw new ExchangeRateNotFoundException("Currency pair not found: " + baseCode + "/" + targetCode);
        }
        return exchangeRate.get();
    }

    public ExchangeRate addExchangeRate(String baseCode, String targetCode, BigDecimal rate) throws SQLException {
        baseCode = baseCode.toUpperCase();
        targetCode = targetCode.toUpperCase();
        Currency baseCurrency = currencyService.findCurrencyByCode(baseCode);
        Currency targetCurrency = currencyService.findCurrencyByCode(targetCode);

        Optional<ExchangeRate> currencyPairsFound = exchangeRateDAO.findExchangeRateByCodes(baseCode, targetCode);
        if (currencyPairsFound.isPresent()) {
            throw new ExchangeRateAlreadyExistsException("Currency pair already exists: " + baseCode + "/" + targetCode);
        }

        ExchangeRate exchangeRate = new ExchangeRate(baseCurrency, targetCurrency, rate);
        Long id = exchangeRateDAO.addExchangeRate(exchangeRate);
        exchangeRate.setId(id);
        return exchangeRate;
    }

    public ExchangeRate updateExchangeRate(String baseCode, String targetCode, BigDecimal rate) throws SQLException {
        baseCode = baseCode.toUpperCase();
        targetCode = targetCode.toUpperCase();

        ExchangeRate exchangeRate = findExchangeRateByCodes(baseCode, targetCode);
        exchangeRate.setRate(rate);
        exchangeRateDAO.updateExchangeRate(exchangeRate);
        return exchangeRate;
    }
}
