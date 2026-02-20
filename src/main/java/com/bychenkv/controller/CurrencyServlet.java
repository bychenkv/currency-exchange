package com.bychenkv.controller;

import com.bychenkv.dto.response.CurrencyResponseDto;
import com.bychenkv.service.CurrencyService;
import com.bychenkv.utils.RequestUtils;
import com.bychenkv.utils.ResponseUtils;
import com.bychenkv.utils.ValidationUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {
    private CurrencyService currencyService;

    @Override
    public void init() {
        this.currencyService = (CurrencyService) getServletContext()
                .getAttribute("currencyService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String rawCode = RequestUtils.getPathParameter(req, "code");
        String code = ValidationUtils.validateCurrencyCode(rawCode);

        CurrencyResponseDto currency = currencyService.findByCode(code);
        ResponseUtils.sendJson(resp, HttpServletResponse.SC_OK, currency);
    }
}
