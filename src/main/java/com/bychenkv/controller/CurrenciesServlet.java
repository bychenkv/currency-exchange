package com.bychenkv.controller;

import com.bychenkv.dao.CurrencyDao;
import com.bychenkv.model.Currency;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/currencies")
public class CurrenciesServlet extends BaseServlet {
    private CurrencyDao dao;

    @Override
    public void init() {
        super.init();
        this.dao = (CurrencyDao) getServletContext().getAttribute("currencyDao");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<Currency> currencies = dao.findAll();
        sendJson(resp, HttpServletResponse.SC_OK, currencies);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code = getRequiredParameter(req, "code");
        String name = getRequiredParameter(req, "name");
        String sign = getRequiredParameter(req, "sign");
        Currency currency = dao.save(new Currency(code, name, sign));
        sendJson(resp, HttpServletResponse.SC_CREATED, currency);
    }
}
