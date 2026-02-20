package com.bychenkv.dto.request;

import java.math.BigDecimal;

public record ExchangeRateRequestDto(
        String base,
        String target,
        BigDecimal rate
) {}
