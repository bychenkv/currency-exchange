package com.bychenkv.dto;

import com.bychenkv.model.Currency;
import com.bychenkv.model.ExchangeRate;

public record ExchangeResult(
        Currency baseCurrency,
        Currency targetCurrency,
        double rate,
        double amount,
        double convertedAmount
) {
    public static ExchangeResult direct(ExchangeRate exchangeRate, double amount) {
        return new ExchangeResult(
                exchangeRate.getBaseCurrency(),
                exchangeRate.getTargetCurrency(),
                exchangeRate.getRate(),
                amount,
                exchangeRate.getRate() * amount
        );
    }

    public static ExchangeResult reversed(ExchangeRate exchangeRate, double amount) {
        return new ExchangeResult(
                exchangeRate.getTargetCurrency(),
                exchangeRate.getBaseCurrency(),
                1 / exchangeRate.getRate(),
                amount,
                amount / exchangeRate.getRate()
        );
    }

    public static ExchangeResult cross(ExchangeRate crossBase,
                                       ExchangeRate crossTarget,
                                       double amount) {
        return new ExchangeResult(
                crossBase.getTargetCurrency(),
                crossTarget.getTargetCurrency(),
                crossTarget.getRate() / crossBase.getRate(),
                amount,
                amount * crossTarget.getRate() / crossBase.getRate()
        );
    }
}
