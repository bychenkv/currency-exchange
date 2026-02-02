package com.bychenkv.controller;

import com.bychenkv.dto.CurrencyCodePair;
import com.bychenkv.dto.ExchangeRateResponseDto;
import com.bychenkv.service.ExchangeRateService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends BaseServlet {
    private ExchangeRateService exchangeRateService;

    @Override
    public void init() {
        super.init();
        this.exchangeRateService = (ExchangeRateService) getServletContext().getAttribute("exchangeRateService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<ExchangeRateResponseDto> exchangeRates = exchangeRateService.findAll();
        sendJson(resp, HttpServletResponse.SC_OK, exchangeRates);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CurrencyCodePair codePair = new CurrencyCodePair(
                getCurrencyCodeParameter(req, "baseCurrencyCode"),
                getCurrencyCodeParameter(req, "targetCurrencyCode")
        );
        BigDecimal rate = getRateParameter(req);

        ExchangeRateResponseDto exchangeRate = exchangeRateService.save(codePair, rate);
        sendJson(resp, HttpServletResponse.SC_CREATED, exchangeRate);
    }
}
