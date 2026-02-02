package com.bychenkv.dto;

import com.bychenkv.model.Currency;
import com.bychenkv.model.ExchangeRate;

import java.math.BigDecimal;

public record ExchangeRateResponseDto(
        int id,
        Currency baseCurrency,
        Currency targetCurrency,
        BigDecimal rate
) {
    public static ExchangeRateResponseDto fromExchangeRate(ExchangeRate exchangeRate) {
        return new ExchangeRateResponseDto(
                exchangeRate.getId(),
                exchangeRate.getBaseCurrency(),
                exchangeRate.getTargetCurrency(),
                exchangeRate.getRate()
        );
    }
}
