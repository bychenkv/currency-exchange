package com.bychenkv.controller;

import com.bychenkv.dto.CurrencyCodePair;
import com.bychenkv.dto.ExchangeResult;
import com.bychenkv.service.ExchangeService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/exchange")
public class ExchangeServlet extends BaseServlet {
    private ExchangeService exchangeService;

    @Override
    public void init() {
        super.init();
        this.exchangeService = (ExchangeService) getServletContext().getAttribute("exchangeService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String from = getCurrencyCodeParameter(req, "from");
        String to = getCurrencyCodeParameter(req, "to");
        BigDecimal amount = getAmountParameter(req);

        ExchangeResult result = exchangeService.exchange(new CurrencyCodePair(from, to), amount);
        sendJson(resp, HttpServletResponse.SC_OK, result);
    }
}
