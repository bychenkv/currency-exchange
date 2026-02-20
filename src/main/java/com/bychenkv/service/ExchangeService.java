package com.bychenkv.service;

import com.bychenkv.dao.ExchangeRateDao;
import com.bychenkv.dto.response.CurrencyResponseDto;
import com.bychenkv.model.Currency;
import com.bychenkv.model.CurrencyCodePair;
import com.bychenkv.dto.request.ExchangeRequestDto;
import com.bychenkv.dto.response.ExchangeResponseDto;
import com.bychenkv.exception.ExchangeRateNotFoundException;
import com.bychenkv.model.ExchangeRate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class ExchangeService {
    private static final String USD = "USD";
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final int RATE_SCALE = 6;
    private static final int AMOUNT_SCALE = 2;

    private final ExchangeRateDao exchangeRateDao;

    public ExchangeService(ExchangeRateDao exchangeRateDao) {
        this.exchangeRateDao = exchangeRateDao;
    }

    public ExchangeResponseDto exchange(ExchangeRequestDto request) {
        CurrencyCodePair codePair = new CurrencyCodePair(request.from(), request.to());

        return exchangeByDirectRate(codePair, request.amount())
                .or(() -> exchangeByReversedRate(codePair, request.amount()))
                .or(() -> exchangeByCrossRate(codePair, request.amount()))
                .orElseThrow(() -> new ExchangeRateNotFoundException(codePair));
    }
    
    private Optional<ExchangeResponseDto> exchangeByDirectRate(CurrencyCodePair codePair,
                                                               BigDecimal amount) {
        return exchangeRateDao.findByCodePair(codePair)
                .map(er -> buildExchangeResponse(
                        er.getBaseCurrency(),
                        er.getTargetCurrency(),
                        er.getRate(),
                        amount
                ));
    }
    
    private Optional<ExchangeResponseDto> exchangeByReversedRate(CurrencyCodePair codePair,
                                                                 BigDecimal amount) {
        return exchangeRateDao.findByCodePair(codePair.reversed())
                .map(er -> buildReverseExchangeResponse(er, amount));
    }

    private Optional<ExchangeResponseDto> exchangeByCrossRate(CurrencyCodePair codePair,
                                                              BigDecimal amount) {
        CurrencyCodePair usdBase = new CurrencyCodePair(USD, codePair.base());
        CurrencyCodePair usdTarget = new CurrencyCodePair(USD, codePair.target());

        return exchangeRateDao.findByCodePair(usdBase)
                .flatMap(usdToBase ->
                        exchangeRateDao.findByCodePair(usdTarget)
                                .map(usdToTarget ->
                                        buildCrossExchangeResponse(usdToBase, usdToTarget, amount)
                                )
                );
    }

    private static ExchangeResponseDto buildReverseExchangeResponse(ExchangeRate exchangeRate,
                                                                    BigDecimal amount) {
        BigDecimal reversedRate = BigDecimal.ONE
                .divide(exchangeRate.getRate(), RATE_SCALE, ROUNDING_MODE);

        return buildExchangeResponse(
                exchangeRate.getTargetCurrency(),
                exchangeRate.getBaseCurrency(),
                reversedRate,
                amount
        );
    }

    private static ExchangeResponseDto buildCrossExchangeResponse(ExchangeRate usdToBase,
                                                                  ExchangeRate usdToTarget,
                                                                  BigDecimal amount) {
        BigDecimal crossRate = usdToTarget.getRate()
                .divide(usdToBase.getRate(), RATE_SCALE, ROUNDING_MODE);

        return buildExchangeResponse(
                usdToBase.getTargetCurrency(),
                usdToTarget.getTargetCurrency(),
                crossRate,
                amount
        );
    }

    private static ExchangeResponseDto buildExchangeResponse(Currency base,
                                                             Currency target,
                                                             BigDecimal rate,
                                                             BigDecimal amount) {
        BigDecimal scaledRate = rate.setScale(RATE_SCALE, ROUNDING_MODE);
        BigDecimal scaledAmount = amount.setScale(AMOUNT_SCALE, ROUNDING_MODE);
        BigDecimal convertedAmount = amount.multiply(rate)
                .setScale(AMOUNT_SCALE, ROUNDING_MODE);

        return new ExchangeResponseDto(
                CurrencyResponseDto.fromCurrency(base),
                CurrencyResponseDto.fromCurrency(target),
                scaledRate,
                scaledAmount,
                convertedAmount
        );
    }
}
