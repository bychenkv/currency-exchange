package com.bychenkv.controller;

import com.bychenkv.dto.CurrencyCodePair;
import com.bychenkv.dto.ExchangeRateResponseDto;
import com.bychenkv.service.ExchangeRateService;
import com.bychenkv.utils.RequestUtils;
import com.bychenkv.utils.ResponseUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private ExchangeRateService exchangeRateService;

    @Override
    public void init() {
        this.exchangeRateService = (ExchangeRateService) getServletContext().getAttribute("exchangeRateService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<ExchangeRateResponseDto> exchangeRates = exchangeRateService.findAll();
        ResponseUtils.sendJson(resp, HttpServletResponse.SC_OK, exchangeRates);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CurrencyCodePair codePair = new CurrencyCodePair(
                RequestUtils.getCurrencyCodeParameter(req, "baseCurrencyCode"),
                RequestUtils.getCurrencyCodeParameter(req, "targetCurrencyCode")
        );
        BigDecimal rate = RequestUtils.getRateParameter(req);

        ExchangeRateResponseDto exchangeRate = exchangeRateService.save(codePair, rate);
        ResponseUtils.sendJson(resp, HttpServletResponse.SC_CREATED, exchangeRate);
    }
}
