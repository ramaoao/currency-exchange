package com.example.currencyexchange.model.errors;

public class CurrencyNotFoundException extends RuntimeException{
    public CurrencyNotFoundException(String message) {
        super(message);
    }
}
