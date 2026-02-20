package com.bychenkv.dto.request;

public record CurrencyRequestDto(
        String code,
        String name,
        String sign
) {}
