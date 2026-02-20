package com.bychenkv.service;

import com.bychenkv.dao.CurrencyDao;
import com.bychenkv.dao.ExchangeRateDao;
import com.bychenkv.model.CurrencyCodePair;
import com.bychenkv.dto.request.ExchangeRateRequestDto;
import com.bychenkv.dto.response.ExchangeRateResponseDto;
import com.bychenkv.exception.CurrencyNotFoundException;
import com.bychenkv.exception.DatabaseException;
import com.bychenkv.exception.ExchangeRateNotFoundException;
import com.bychenkv.model.Currency;

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

    public ExchangeRateResponseDto save(ExchangeRateRequestDto exchangeRate) {
        Currency base = currencyDao.findByCode(exchangeRate.base())
                .orElseThrow(() -> new CurrencyNotFoundException(exchangeRate.base()));

        Currency target = currencyDao.findByCode(exchangeRate.target())
                .orElseThrow(() -> new CurrencyNotFoundException(exchangeRate.target()));

        int id = exchangeRateDao.save(base, target, exchangeRate.rate());

        return new ExchangeRateResponseDto(id, base, target, exchangeRate.rate());
    }

    public ExchangeRateResponseDto update(ExchangeRateRequestDto exchangeRate) {
        CurrencyCodePair codePair = new CurrencyCodePair(exchangeRate.base(), exchangeRate.target());

        Currency baseCurrency = currencyDao.findByCode(exchangeRate.base())
                .orElseThrow(() -> new CurrencyNotFoundException(exchangeRate.base()));

        Currency targetCurrency = currencyDao.findByCode(exchangeRate.target())
                .orElseThrow(() -> new CurrencyNotFoundException(exchangeRate.target()));

        exchangeRateDao.update(baseCurrency, targetCurrency, exchangeRate.rate());

        return exchangeRateDao.findByCodePair(codePair)
                .map(ExchangeRateResponseDto::fromExchangeRate)
                .orElseThrow(() -> new DatabaseException("Error retrieving updated exchange rate"));
    }
}
