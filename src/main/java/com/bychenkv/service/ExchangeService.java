package com.bychenkv.service;

import com.bychenkv.dao.ExchangeRateDao;
import com.bychenkv.dto.CurrencyCodePair;
import com.bychenkv.dto.ExchangeResponseDto;
import com.bychenkv.exception.ExchangeRateNotFoundException;
import com.bychenkv.model.ExchangeRate;

import java.math.BigDecimal;
import java.util.Optional;

public class ExchangeService {
    private static final String USD = "USD";

    private final ExchangeRateDao exchangeRateDao;

    public ExchangeService(ExchangeRateDao exchangeRateDao) {
        this.exchangeRateDao = exchangeRateDao;
    }

    public ExchangeResponseDto exchange(CurrencyCodePair codePair, BigDecimal amount) {
        Optional<ExchangeResponseDto> direct = exchangeRateDao.findByCodePair(codePair)
                .map(er -> ExchangeResponseDto.direct(er, amount));
        if (direct.isPresent()) {
            return direct.get();
        }

        Optional<ExchangeResponseDto> reversed = exchangeRateDao.findByCodePair(codePair.reversed())
                .map(er -> ExchangeResponseDto.reversed(er, amount));

        return reversed.orElseGet(() -> exchangeByCrossRate(codePair, amount)
                .orElseThrow(() -> new ExchangeRateNotFoundException(codePair)));

    }

    private Optional<ExchangeResponseDto> exchangeByCrossRate(CurrencyCodePair codePair, BigDecimal amount) {
        CurrencyCodePair usdBase = new CurrencyCodePair(USD, codePair.base());
        CurrencyCodePair usdTarget = new CurrencyCodePair(USD, codePair.target());

        Optional<ExchangeRate> usdToBase = exchangeRateDao.findByCodePair(usdBase);
        Optional<ExchangeRate> usdToTarget = exchangeRateDao.findByCodePair(usdTarget);

        if (usdToBase.isPresent() && usdToTarget.isPresent()) {
            return Optional.of(
                    ExchangeResponseDto.cross(usdToBase.get(), usdToTarget.get(), amount)
            );
        }
        return Optional.empty();
    }
}
