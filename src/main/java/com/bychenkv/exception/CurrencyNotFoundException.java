package com.bychenkv.exception;

public class CurrencyNotFoundException extends Exception {
    public CurrencyNotFoundException(String code) {
        super("Currency " + code + " not found");
    }
}
