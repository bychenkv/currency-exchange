package com.bychenkv.dto;

public record CurrencyCodePair(String base, String target) {
    public CurrencyCodePair(String codePair) {
        this(codePair.substring(0, 3), codePair.substring(3, 6));
    }

    @Override
    public String toString() {
        return base + target;
    }

    public CurrencyCodePair reversed() {
        return new CurrencyCodePair(target, base);
    }
}
