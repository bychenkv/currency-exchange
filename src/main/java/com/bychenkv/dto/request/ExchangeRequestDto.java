package com.bychenkv.dto.request;

import java.math.BigDecimal;

public record ExchangeRequestDto(String from, String to, BigDecimal amount) { }
