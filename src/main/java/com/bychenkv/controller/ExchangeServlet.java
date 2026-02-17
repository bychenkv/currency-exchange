package com.bychenkv.controller;

import com.bychenkv.dto.CurrencyCodePair;
import com.bychenkv.dto.ExchangeResponseDto;
import com.bychenkv.service.ExchangeService;
import com.bychenkv.utils.RequestUtils;
import com.bychenkv.utils.ResponseUtils;
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
        this.exchangeService = (ExchangeService) getServletContext().getAttribute("exchangeService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String from = RequestUtils.getCurrencyCodeParameter(req, "from");
        String to = RequestUtils.getCurrencyCodeParameter(req, "to");
        BigDecimal amount = RequestUtils.getAmountParameter(req);

        ExchangeResponseDto result = exchangeService.exchange(new CurrencyCodePair(from, to), amount);
        ResponseUtils.sendJson(resp, HttpServletResponse.SC_OK, result);
    }
}
