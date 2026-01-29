package com.bychenkv.controller;

import com.bychenkv.dao.ExchangeRateDao;
import com.bychenkv.dto.CurrencyCodePair;
import com.bychenkv.model.ExchangeRate;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends BaseServlet {
    private ExchangeRateDao dao;

    @Override
    public void init() {
        super.init();
        this.dao = (ExchangeRateDao) getServletContext().getAttribute("exchangeRateDao");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<ExchangeRate> exchangeRates = dao.findAll();
        sendJson(resp, HttpServletResponse.SC_OK, exchangeRates);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String base = getCurrencyCodeParameter(req, "baseCurrencyCode");
        String target = getCurrencyCodeParameter(req, "targetCurrencyCode");
        BigDecimal rate = getRateParameter(req);

        ExchangeRate exchangeRate = dao.save(new CurrencyCodePair(base, target), rate);
        sendJson(resp, HttpServletResponse.SC_CREATED, exchangeRate);
    }
}
