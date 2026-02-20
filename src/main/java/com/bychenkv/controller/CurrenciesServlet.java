package com.bychenkv.controller;

import com.bychenkv.dto.request.CurrencyRequestDto;
import com.bychenkv.dto.response.CurrencyResponseDto;
import com.bychenkv.service.CurrencyService;
import com.bychenkv.utils.RequestParams;
import com.bychenkv.utils.ResponseUtils;
import com.bychenkv.utils.ValidationUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {
    private CurrencyService currencyService;

    @Override
    public void init() {
        this.currencyService = (CurrencyService) getServletContext()
                .getAttribute("currencyService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<CurrencyResponseDto> currencies = currencyService.findAll();
        ResponseUtils.sendJson(resp, HttpServletResponse.SC_OK, currencies);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CurrencyRequestDto currency = parsePostRequest(req);
        CurrencyResponseDto saved = currencyService.save(currency);
        ResponseUtils.sendJson(resp, HttpServletResponse.SC_CREATED, saved);
    }

    private static CurrencyRequestDto parsePostRequest(HttpServletRequest req) throws IOException {
        RequestParams params = RequestParams.from(req);
        return new CurrencyRequestDto(
                ValidationUtils.validateCurrencyCode(params.requireRaw("code")),
                params.requireRaw("name"),
                params.requireRaw("sign")
        );
    }
}
