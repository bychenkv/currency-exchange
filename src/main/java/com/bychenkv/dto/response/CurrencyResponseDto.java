package com.bychenkv.dto.response;

import com.bychenkv.model.Currency;

public record CurrencyResponseDto(
        int id,
        String code,
        String name,
        String sign
) {
    public static CurrencyResponseDto fromCurrency(Currency currency) {
        return new CurrencyResponseDto(
                currency.getId(),
                currency.getCode(),
                currency.getFullName(),
                currency.getSign()
        );
    }
}
