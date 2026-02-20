package com.bychenkv.controller;

import com.bychenkv.dto.request.ExchangeRateRequestDto;
import com.bychenkv.dto.response.ExchangeRateResponseDto;
import com.bychenkv.service.ExchangeRateService;
import com.bychenkv.utils.RequestParams;
import com.bychenkv.utils.ResponseUtils;
import com.bychenkv.utils.ValidationUtils;
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
        this.exchangeRateService = (ExchangeRateService) getServletContext()
                .getAttribute("exchangeRateService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<ExchangeRateResponseDto> exchangeRates = exchangeRateService.findAll();
        ResponseUtils.sendJson(resp, HttpServletResponse.SC_OK, exchangeRates);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ExchangeRateRequestDto exchangeRate = parsePostRequest(req);
        ExchangeRateResponseDto saved = exchangeRateService.save(exchangeRate);
        ResponseUtils.sendJson(resp, HttpServletResponse.SC_CREATED, saved);
    }

    private static ExchangeRateRequestDto parsePostRequest(HttpServletRequest req) throws IOException {
        RequestParams params = RequestParams.from(req);

        String base = params.requireRaw("baseCurrencyCode");
        String target = params.requireRaw("targetCurrencyCode");
        BigDecimal rate = params.requireDecimal("rate");

        return new ExchangeRateRequestDto(
                ValidationUtils.validateCurrencyCode(base),
                ValidationUtils.validateCurrencyCode(target),
                ValidationUtils.validateRate(rate)
        );
    }
}
