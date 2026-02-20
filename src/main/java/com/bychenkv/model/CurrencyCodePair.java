package com.bychenkv.model;

public record CurrencyCodePair(String base, String target) {
    @Override
    public String toString() {
        return base + target;
    }

    public CurrencyCodePair reversed() {
        return new CurrencyCodePair(target, base);
    }
}
