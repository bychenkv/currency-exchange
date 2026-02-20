package com.bychenkv.controller;

import com.bychenkv.model.CurrencyCodePair;
import com.bychenkv.dto.request.ExchangeRateRequestDto;
import com.bychenkv.dto.response.ExchangeRateResponseDto;
import com.bychenkv.service.ExchangeRateService;
import com.bychenkv.utils.RequestParams;
import com.bychenkv.utils.RequestUtils;
import com.bychenkv.utils.ResponseUtils;
import com.bychenkv.utils.ValidationUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private ExchangeRateService exchangeRateService;

    @Override
    public void init() {
        this.exchangeRateService = (ExchangeRateService) getServletContext()
                .getAttribute("exchangeRateService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CurrencyCodePair codePair = extractCodePair(req);
        ExchangeRateResponseDto exchangeRate = exchangeRateService.findByCodePair(codePair);
        ResponseUtils.sendJson(resp, HttpServletResponse.SC_OK, exchangeRate);
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ExchangeRateRequestDto exchangeRate = parsePatchRequest(req);
        ExchangeRateResponseDto updated = exchangeRateService.update(exchangeRate);
        ResponseUtils.sendJson(resp, HttpServletResponse.SC_OK, updated);
    }

    private static ExchangeRateRequestDto parsePatchRequest(HttpServletRequest req) throws IOException {
        CurrencyCodePair codePair = extractCodePair(req);
        BigDecimal rate = RequestParams.from(req).requireDecimal("rate");

        return new ExchangeRateRequestDto(
                codePair.base(),
                codePair.target(),
                ValidationUtils.validateRate(rate)
        );
    }

    private static CurrencyCodePair extractCodePair(HttpServletRequest req) {
        String codePair = RequestUtils.getPathParameter(req, "currencyCodePair");
        return ValidationUtils.validateCurrencyCodePair(codePair);
    }
}
