package com.bychenkv.controller;

import com.bychenkv.dto.CurrencyResponseDto;
import com.bychenkv.service.CurrencyService;
import com.bychenkv.utils.ResponseUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/currency/*")
public class CurrencyServlet extends BaseServlet {
    private CurrencyService currencyService;

    @Override
    public void init() {
        this.currencyService = (CurrencyService) getServletContext().getAttribute("currencyService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code = getPathParameter(req, "code").toUpperCase();
        if (!code.matches(VALID_CURRENCY_CODE_REGEX)) {
            throw new IllegalArgumentException("Invalid currency code: " + code);
        }
        CurrencyResponseDto currency = currencyService.findByCode(code);
        ResponseUtils.sendJson(resp, HttpServletResponse.SC_OK, currency);
    }
}
