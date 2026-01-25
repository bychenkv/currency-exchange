package com.bychenkv.service;

import com.bychenkv.dao.ExchangeRateDao;
import com.bychenkv.dto.CurrencyCodePair;
import com.bychenkv.dto.ExchangeResult;
import com.bychenkv.exception.ExchangeRateNotFoundException;
import com.bychenkv.model.ExchangeRate;

import java.sql.SQLException;
import java.util.Optional;

public class ExchangeService {
    private final ExchangeRateDao dao;

    public ExchangeService(ExchangeRateDao dao) {
        this.dao = dao;
    }

    public ExchangeResult exchange(CurrencyCodePair codePair,
                                   double amount) throws SQLException,
                                                         ExchangeRateNotFoundException {
        Optional<ExchangeRate> direct = dao.findByCodePair(codePair);
        if (direct.isPresent()) {
            ExchangeRate exchangeRate = direct.get();
            return new ExchangeResult(
                    exchangeRate.getBaseCurrency(),
                    exchangeRate.getTargetCurrency(),
                    exchangeRate.getRate(),
                    amount,
                    exchangeRate.getRate() * amount
            );
        }

        Optional<ExchangeRate> reverse = dao.findByCodePair(codePair.reversed());
        if (reverse.isPresent()) {
            ExchangeRate exchangeRate = reverse.get();
            return new ExchangeResult(
                    exchangeRate.getBaseCurrency(),
                    exchangeRate.getTargetCurrency(),
                    1 / exchangeRate.getRate(),
                    amount,
                    amount / exchangeRate.getRate()
            );
        }

        Optional<ExchangeRate> crossBase = dao.findByCodePair(
                new CurrencyCodePair("USD", codePair.base())
        );
        if (crossBase.isPresent()) {
            Optional<ExchangeRate> crossTarget = dao.findByCodePair(
                    new CurrencyCodePair("USD", codePair.target())
            );
            if (crossTarget.isPresent()) {
                ExchangeRate crossBaseExchangeRate = crossBase.get();
                ExchangeRate crossTargetExchangeRate = crossTarget.get();

                return new ExchangeResult(
                        crossBaseExchangeRate.getTargetCurrency(),
                        crossTargetExchangeRate.getTargetCurrency(),
                        crossTargetExchangeRate.getRate() / crossBaseExchangeRate.getRate(),
                        amount,
                        amount * crossTargetExchangeRate.getRate() / crossBaseExchangeRate.getRate()
                );
            }
        }

        throw new ExchangeRateNotFoundException(codePair);
    }
}
