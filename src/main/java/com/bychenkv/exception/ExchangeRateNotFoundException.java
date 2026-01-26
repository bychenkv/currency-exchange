package com.bychenkv.exception;

import com.bychenkv.dto.CurrencyCodePair;

public class ExchangeRateNotFoundException extends RuntimeException {
    public ExchangeRateNotFoundException(CurrencyCodePair codePair) {
        super("Exchange rate for " + codePair + " not found");
    }
}
