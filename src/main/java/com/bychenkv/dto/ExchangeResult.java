package com.bychenkv.dto;

import com.bychenkv.model.Currency;

public record ExchangeResult(
        Currency baseCurrency,
        Currency targetCurrency,
        double rate,
        double amount,
        double convertedAmount
) {}
