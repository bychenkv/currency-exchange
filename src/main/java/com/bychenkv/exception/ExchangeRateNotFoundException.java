package com.bychenkv.exception;

import com.bychenkv.model.CurrencyCodePair;

public class ExchangeRateNotFoundException extends Exception {
    public ExchangeRateNotFoundException(CurrencyCodePair codePair) {
        super("Exchange rate for " + codePair + " not found");
    }
}
