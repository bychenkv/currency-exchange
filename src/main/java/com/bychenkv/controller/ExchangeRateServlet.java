package com.bychenkv.controller;

import com.bychenkv.dto.CurrencyCodePair;
import com.bychenkv.dto.ExchangeRateResponseDto;
import com.bychenkv.service.ExchangeRateService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends BaseServlet {
    private static final String VALID_CURRENCY_CODE_PAIR_REGEX = "^[A-Z]{6}$";

    private ExchangeRateService exchangeRateService;

    @Override
    public void init() {
        super.init();
        this.exchangeRateService = (ExchangeRateService) getServletContext().getAttribute("exchangeRateService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CurrencyCodePair codePair = extractCodePair(req);
        ExchangeRateResponseDto exchangeRate = exchangeRateService.findByCodePair(codePair);
        sendJson(resp, HttpServletResponse.SC_OK, exchangeRate);
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CurrencyCodePair codePair = extractCodePair(req);
        BigDecimal rate = getRateParameter(req);

        ExchangeRateResponseDto exchangeRate = exchangeRateService.update(codePair, rate);
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
