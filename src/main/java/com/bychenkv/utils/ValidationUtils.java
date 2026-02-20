package com.bychenkv.utils;

import com.bychenkv.model.CurrencyCodePair;

import java.math.BigDecimal;

public final class ValidationUtils {
    private static final int CURRENCY_CODE_LENGTH = 3;
    private static final String VALID_CURRENCY_CODE_REGEX = "^[A-Z]{3}$";
    private static final String VALID_CURRENCY_CODE_PAIR_REGEX = "^[A-Z]{6}$";

    private ValidationUtils() {}

    public static String validateCurrencyCode(String rawCode) {
        String code = rawCode.toUpperCase().trim();
        if (!code.matches(VALID_CURRENCY_CODE_REGEX)) {
            throw new IllegalArgumentException("Invalid currency code: " + rawCode);
        }
        return code;
    }

    public static CurrencyCodePair validateCurrencyCodePair(String rawCodePair) {
        String codePair = rawCodePair.toUpperCase().trim();
        if (!codePair.matches(VALID_CURRENCY_CODE_PAIR_REGEX)) {
            throw new IllegalArgumentException("Invalid currency code pair: " + codePair);
        }

        String base = codePair.substring(0, CURRENCY_CODE_LENGTH);
        String target = codePair.substring(CURRENCY_CODE_LENGTH);

        return new CurrencyCodePair(base, target);
    }

    public static BigDecimal validateRate(BigDecimal rate) {
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Rate must be positive");
        }
        return rate;
    }

    public static BigDecimal validateAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be non negative");
        }
        return amount;
    }
}
