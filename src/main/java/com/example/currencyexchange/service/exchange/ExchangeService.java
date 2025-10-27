package com.example.currencyexchange.service.exchange;

import com.example.currencyexchange.dao.ExchangeRateDAO;
import com.example.currencyexchange.model.entity.ExchangeRate;
import com.example.currencyexchange.model.errors.ExchangeRateNotFoundException;
import com.example.currencyexchange.model.response.ExchangeResponse;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

import static java.math.RoundingMode.HALF_EVEN;

public class ExchangeService {
    private final ExchangeRateDAO exchangeRateDAO = new ExchangeRateDAO();

    public ExchangeResponse convertCurrency(String baseCurrency, String targetCurrency, BigDecimal amount) throws SQLException {
        baseCurrency = baseCurrency.toUpperCase();
        targetCurrency = targetCurrency.toUpperCase();

        Optional<ExchangeRate> optionalExchangeRate = getExchangeRate(baseCurrency, targetCurrency);
        if (optionalExchangeRate.isEmpty()) {
            throw new ExchangeRateNotFoundException("The exchange rate for this pair is not found: " + baseCurrency + "/" + targetCurrency);
        }

        ExchangeRate exchangeRate = optionalExchangeRate.get();
        BigDecimal convertedAmount = exchangeRate.getRate()
                .multiply(amount)
                .setScale(2, HALF_EVEN);

        return new ExchangeResponse (
                exchangeRate.getBaseCurrency(),
                exchangeRate.getTargetCurrency(),
                exchangeRate.getRate(),
                amount,
                convertedAmount
        );
    }

    private Optional<ExchangeRate> getExchangeRate(String baseCurrency, String targetCurrency) throws SQLException {
        Optional<ExchangeRate> exchangeRate = getDirectRate(baseCurrency, targetCurrency);

        if (exchangeRate.isEmpty()) {
            exchangeRate = getReverseRate(baseCurrency, targetCurrency);
        }

        if (exchangeRate.isEmpty()) {
            exchangeRate = getCrossRateViaUsd(baseCurrency, targetCurrency);
        }

        return exchangeRate;
    }

    private Optional<ExchangeRate> getDirectRate(String baseCurrency, String targetCurrency) throws SQLException {
        return exchangeRateDAO.findExchangeRateByCodes(baseCurrency, targetCurrency);
    }

    private Optional<ExchangeRate> getReverseRate(String baseCurrency, String targetCurrency) throws SQLException {
        Optional<ExchangeRate> getExchangeRate = exchangeRateDAO.findExchangeRateByCodes(targetCurrency, baseCurrency);

        if (getExchangeRate.isEmpty()) {
            return Optional.empty();
        }

        ExchangeRate reverse = getExchangeRate.get();

        return Optional.of(new ExchangeRate(
                reverse.getTargetCurrency(),
                reverse.getBaseCurrency(),
                BigDecimal.ONE.divide(reverse.getRate(), 10, HALF_EVEN)
        ));
    }

    private Optional<ExchangeRate> getCrossRateViaUsd(String baseCurrency, String targetCurrency) throws SQLException {
        if ("USD".equals(baseCurrency) || "USD".equals(targetCurrency)) {
            return Optional.empty();
        }

        Optional<ExchangeRate> usdToBaseOptional = exchangeRateDAO.findExchangeRateByCodes("USD", baseCurrency);
        if (usdToBaseOptional.isEmpty()) {
            Optional<ExchangeRate> baseToUsdOptional = exchangeRateDAO.findExchangeRateByCodes(baseCurrency, "USD");
            if (baseToUsdOptional.isEmpty()) {
                return Optional.empty();
            }

            ExchangeRate baseToUsd = baseToUsdOptional.get();
            BigDecimal inverted = BigDecimal.ONE.divide(baseToUsd.getRate(), 10, HALF_EVEN);

            usdToBaseOptional = Optional.of(new ExchangeRate(baseToUsd.getTargetCurrency(), baseToUsd.getBaseCurrency(), inverted));
        }

        Optional<ExchangeRate> usdToTargetOptional = exchangeRateDAO.findExchangeRateByCodes("USD", targetCurrency);
        if (usdToTargetOptional.isEmpty()) {
            Optional<ExchangeRate> targetToUsdOptional = exchangeRateDAO.findExchangeRateByCodes(targetCurrency, "USD");
            if (targetToUsdOptional.isEmpty()) {
                return Optional.empty();
            }

            ExchangeRate targetToUsd = targetToUsdOptional.get();
            BigDecimal inverted = BigDecimal.ONE.divide(targetToUsd.getRate(), 10, HALF_EVEN);

            usdToBaseOptional = Optional.of(new ExchangeRate(targetToUsd.getTargetCurrency(), targetToUsd.getBaseCurrency(), inverted));
        }

        ExchangeRate usdToBase = usdToBaseOptional.get();
        ExchangeRate usdToTarget = usdToTargetOptional.get();

        BigDecimal crossRate = usdToTarget.getRate().divide(usdToBase.getRate(), 10, HALF_EVEN);

        return Optional.of(new ExchangeRate(
                usdToBase.getTargetCurrency(),
                usdToTarget.getTargetCurrency(),
                crossRate
        ));
    }
}
