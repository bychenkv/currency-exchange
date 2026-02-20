package com.bychenkv.exception;

import com.bychenkv.model.CurrencyCodePair;

public class ExchangeRateNotFoundException extends RuntimeException {
    public ExchangeRateNotFoundException(CurrencyCodePair codePair) {
        super("Exchange rate not found: " + codePair);
    }
}
