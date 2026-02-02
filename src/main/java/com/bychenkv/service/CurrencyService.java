package com.bychenkv.service;

import com.bychenkv.dao.CurrencyDao;
import com.bychenkv.dto.CurrencyRequestDto;
import com.bychenkv.dto.CurrencyResponseDto;
import com.bychenkv.exception.CurrencyNotFoundException;
import com.bychenkv.model.Currency;

import java.util.List;

public class CurrencyService {
    private static final String VALID_CURRENCY_CODE_REGEX = "^[A-Z]{3}$";

    private final CurrencyDao currencyDao;

    public CurrencyService(CurrencyDao currencyDao) {
        this.currencyDao = currencyDao;
    }

    public List<CurrencyResponseDto> findAll() {
        return currencyDao.findAll().stream()
                .map(CurrencyResponseDto::fromCurrency)
                .toList();
    }

    public CurrencyResponseDto findByCode(String code) {
        Currency currency = currencyDao.findByCode(code)
                .orElseThrow(() -> new CurrencyNotFoundException(code));

        return CurrencyResponseDto.fromCurrency(currency);
    }

    public CurrencyResponseDto save(CurrencyRequestDto requestDto) {
        int id = currencyDao.save(requestDto);
        return new CurrencyResponseDto(
                id,
                requestDto.code(),
                requestDto.name(),
                requestDto.sign()
        );
    }
}
