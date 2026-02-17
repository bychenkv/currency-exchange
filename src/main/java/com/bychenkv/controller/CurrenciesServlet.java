package com.bychenkv.controller;

import com.bychenkv.dto.CurrencyRequestDto;
import com.bychenkv.dto.CurrencyResponseDto;
import com.bychenkv.service.CurrencyService;
import com.bychenkv.utils.ResponseUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/currencies")
public class CurrenciesServlet extends BaseServlet {
    private CurrencyService currencyService;

    @Override
    public void init() {
        this.currencyService = (CurrencyService) getServletContext().getAttribute("currencyService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<CurrencyResponseDto> currencies = currencyService.findAll();
        ResponseUtils.sendJson(resp, HttpServletResponse.SC_OK, currencies);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CurrencyRequestDto requestDto = new CurrencyRequestDto(
                getRequiredParameter(req, "code"),
                getRequiredParameter(req, "name"),
                getRequiredParameter(req, "sign")
        );
        CurrencyResponseDto currency = currencyService.save(requestDto);
        ResponseUtils.sendJson(resp, HttpServletResponse.SC_CREATED, currency);
    }
}
