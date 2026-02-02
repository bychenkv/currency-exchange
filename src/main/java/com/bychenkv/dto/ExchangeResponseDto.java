package com.bychenkv.dto;

import com.bychenkv.model.Currency;
import com.bychenkv.model.ExchangeRate;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record ExchangeResponseDto(
        Currency baseCurrency,
        Currency targetCurrency,
        BigDecimal rate,
        BigDecimal amount,
        BigDecimal convertedAmount
) {
    private static final int DEFAULT_RATE_SCALE = 6;

    public static ExchangeResponseDto direct(ExchangeRate exchangeRate, BigDecimal amount) {
        return new ExchangeResponseDto(
                exchangeRate.getBaseCurrency(),
                exchangeRate.getTargetCurrency(),
                exchangeRate.getRate(),
                amount,
                amount.multiply(exchangeRate.getRate())
        );
    }

    public static ExchangeResponseDto reversed(ExchangeRate exchangeRate, BigDecimal amount) {
        BigDecimal reversedRate = BigDecimal.ONE.divide(exchangeRate.getRate(),
                DEFAULT_RATE_SCALE,
                RoundingMode.HALF_UP);

        return new ExchangeResponseDto(
                exchangeRate.getTargetCurrency(),
                exchangeRate.getBaseCurrency(),
                reversedRate,
                amount,
                amount.multiply(reversedRate)
        );
    }

    public static ExchangeResponseDto cross(ExchangeRate crossBase,
                                            ExchangeRate crossTarget,
                                            BigDecimal amount) {
        BigDecimal crossRate = crossTarget.getRate().divide(crossBase.getRate(),
                DEFAULT_RATE_SCALE,
                RoundingMode.HALF_UP);

        return new ExchangeResponseDto(
                crossBase.getTargetCurrency(),
                crossTarget.getTargetCurrency(),
                crossRate,
                amount,
                amount.multiply(crossRate)
        );
    }
}
