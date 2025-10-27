package com.example.currencyexchange.model.errors;

public class ExchangeRateNotFoundException extends RuntimeException {
    public ExchangeRateNotFoundException(String message) {
        super(message);
    }
}
