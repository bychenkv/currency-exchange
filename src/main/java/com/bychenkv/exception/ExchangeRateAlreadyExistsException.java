package com.bychenkv.exception;

import com.bychenkv.model.CurrencyCodePair;

public class ExchangeRateAlreadyExistsException extends RuntimeException {
    public ExchangeRateAlreadyExistsException(CurrencyCodePair codePair) {
        super("Exchange rate already exists: " + codePair);
    }
}
