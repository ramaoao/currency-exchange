package com.example.currencyexchange.model.errors;

public class CurrencyAlreadyExistsException extends RuntimeException {
    public CurrencyAlreadyExistsException(String message) {
        super(message);
    }
}
