package com.bychenkv.controller;

import com.bychenkv.dao.ExchangeRateDao;
import com.bychenkv.dto.CurrencyCodePair;
import com.bychenkv.model.ExchangeRate;
import com.bychenkv.utils.ResponseUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends BaseServlet {
    private static final String VALID_CURRENCY_CODE_PAIR_REGEX = "^[A-Z]{6}$";

    private ExchangeRateDao dao;

    @Override
    public void init() {
        super.init();
        this.dao = (ExchangeRateDao) getServletContext().getAttribute("exchangeRateDao");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CurrencyCodePair codePair = extractCodePair(req);

        Optional<ExchangeRate> exchangeRate = dao.findByCodePair(codePair);
        if (exchangeRate.isEmpty()) {
            ResponseUtils.sendError(resp,
                    HttpServletResponse.SC_NOT_FOUND,
                    "Exchange rate for currency pair " + codePair + " not found");
            return;
        }
        sendJson(resp, HttpServletResponse.SC_OK, exchangeRate.get());
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CurrencyCodePair codePair = extractCodePair(req);
        double rate = getRateParameter(req);

        ExchangeRate exchangeRate = dao.update(codePair, rate);
        sendJson(resp, HttpServletResponse.SC_OK, exchangeRate);
    }

    private CurrencyCodePair extractCodePair(HttpServletRequest req) {
        String codePair = getPathParameter(req, "currencyCodePair");
        if (!codePair.matches(VALID_CURRENCY_CODE_PAIR_REGEX)) {
            throw new IllegalArgumentException("Invalid currency code pair: " + codePair);
        }
        return new CurrencyCodePair(codePair);
    }
}
