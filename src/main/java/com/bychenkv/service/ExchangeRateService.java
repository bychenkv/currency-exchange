package com.bychenkv.service;

import com.bychenkv.dao.CurrencyDao;
import com.bychenkv.dao.ExchangeRateDao;
import com.bychenkv.dto.CurrencyCodePair;
import com.bychenkv.dto.ExchangeRateResponseDto;
import com.bychenkv.exception.CurrencyNotFoundException;
import com.bychenkv.exception.DatabaseException;
import com.bychenkv.exception.ExchangeRateNotFoundException;
import com.bychenkv.model.Currency;

import java.math.BigDecimal;
import java.util.List;

public class ExchangeRateService {
    private final ExchangeRateDao exchangeRateDao;
    private final CurrencyDao currencyDao;

    public ExchangeRateService(ExchangeRateDao exchangeRateDao, CurrencyDao currencyDao) {
        this.exchangeRateDao = exchangeRateDao;
        this.currencyDao = currencyDao;
    }

    public List<ExchangeRateResponseDto> findAll() {
        return exchangeRateDao.findAll().stream()
                .map(ExchangeRateResponseDto::fromExchangeRate)
                .toList();
    }

    public ExchangeRateResponseDto findByCodePair(CurrencyCodePair codePair) {
        return exchangeRateDao.findByCodePair(codePair)
                .map(ExchangeRateResponseDto::fromExchangeRate)
                .orElseThrow(() -> new ExchangeRateNotFoundException(codePair));
    }

    public ExchangeRateResponseDto save(CurrencyCodePair codePair, BigDecimal rate) {
        Currency base = currencyDao.findByCode(codePair.base())
                .orElseThrow(() -> new CurrencyNotFoundException(codePair.base()));

        Currency target = currencyDao.findByCode(codePair.target())
                .orElseThrow(() -> new CurrencyNotFoundException(codePair.target()));

        int id = exchangeRateDao.save(base, target, rate);

        return new ExchangeRateResponseDto(id, base, target, rate);
    }

    public ExchangeRateResponseDto update(CurrencyCodePair codePair, BigDecimal rate) {
        Currency baseCurrency = currencyDao.findByCode(codePair.base())
                .orElseThrow(() -> new CurrencyNotFoundException(codePair.base()));

        Currency targetCurrency = currencyDao.findByCode(codePair.target())
                .orElseThrow(() -> new CurrencyNotFoundException(codePair.target()));

        exchangeRateDao.update(baseCurrency, targetCurrency, rate);

        return exchangeRateDao.findByCodePair(codePair)
                .map(ExchangeRateResponseDto::fromExchangeRate)
                .orElseThrow(() -> new DatabaseException("Error retrieving updated exchange rate"));
    }
}
