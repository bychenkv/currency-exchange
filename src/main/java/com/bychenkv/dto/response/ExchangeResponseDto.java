package com.bychenkv.dto.response;

import com.bychenkv.model.Currency;
import com.bychenkv.model.ExchangeRate;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record ExchangeResponseDto(
        CurrencyResponseDto baseCurrency,
        CurrencyResponseDto targetCurrency,
        BigDecimal rate,
        BigDecimal amount,
        BigDecimal convertedAmount
) {
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final int RATE_SCALE = 6;
    private static final int AMOUNT_SCALE = 2;

    public static ExchangeResponseDto direct(ExchangeRate exchangeRate, BigDecimal amount) {
        return create(
                exchangeRate.getBaseCurrency(),
                exchangeRate.getTargetCurrency(),
                exchangeRate.getRate(),
                amount
        );
    }

    public static ExchangeResponseDto reversed(ExchangeRate exchangeRate, BigDecimal amount) {
        BigDecimal reversedRate = BigDecimal.ONE.divide(
                exchangeRate.getRate(),
                RATE_SCALE,
                RoundingMode.HALF_UP
        );

        return create(
                exchangeRate.getTargetCurrency(),
                exchangeRate.getBaseCurrency(),
                reversedRate,
                amount
        );
    }

    public static ExchangeResponseDto cross(ExchangeRate usdToBase,
                                            ExchangeRate usdToTarget,
                                            BigDecimal amount) {
        BigDecimal crossRate = usdToTarget.getRate()
                .divide(usdToBase.getRate(), RATE_SCALE, RoundingMode.HALF_UP);

        return create(
                usdToBase.getTargetCurrency(),
                usdToTarget.getTargetCurrency(),
                crossRate,
                amount
        );
    }

    private static ExchangeResponseDto create(Currency base,
                                              Currency target,
                                              BigDecimal rate,
                                              BigDecimal amount) {
        return new ExchangeResponseDto(
                CurrencyResponseDto.fromCurrency(base),
                CurrencyResponseDto.fromCurrency(target),
                rate.setScale(RATE_SCALE, ROUNDING_MODE),
                amount.setScale(AMOUNT_SCALE, ROUNDING_MODE),
                amount.multiply(rate).setScale(AMOUNT_SCALE, ROUNDING_MODE)
        );
    }
}
