package com.bychenkv.controller;

import com.bychenkv.dto.request.ExchangeRequestDto;
import com.bychenkv.dto.response.ExchangeResponseDto;
import com.bychenkv.service.ExchangeService;
import com.bychenkv.utils.RequestParams;
import com.bychenkv.utils.ResponseUtils;
import com.bychenkv.utils.ValidationUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {
    private ExchangeService exchangeService;

    @Override
    public void init() {
        this.exchangeService = (ExchangeService) getServletContext()
                .getAttribute("exchangeService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ExchangeRequestDto exchangeRequest = parseGetRequest(req);
        ExchangeResponseDto result = exchangeService.exchange(exchangeRequest);
        ResponseUtils.sendJson(resp, HttpServletResponse.SC_OK, result);
    }

    private static ExchangeRequestDto parseGetRequest(HttpServletRequest req) throws IOException {
        RequestParams params = RequestParams.from(req);

        String from = params.requireRaw("from");
        String to = params.requireRaw("to");
        BigDecimal amount = params.requireDecimal("amount");

        return new ExchangeRequestDto(
                ValidationUtils.validateCurrencyCode(from),
                ValidationUtils.validateCurrencyCode(to),
                ValidationUtils.validateAmount(amount)
        );
    }
}
