package com.bychenkv.dto;

public record CurrencyRequestDto(
        String code,
        String name,
        String sign
) {}
