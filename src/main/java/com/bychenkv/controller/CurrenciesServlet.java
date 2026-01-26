package com.bychenkv.controller;

import com.bychenkv.dao.CurrencyDao;
import com.bychenkv.exception.MissingParameterException;
import com.bychenkv.model.Currency;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

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
        sendJson(resp, HttpServletResponse.SC_OK, dao.findAll());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Currency currency = getCurrencyFromRequest(req);
        sendJson(resp, HttpServletResponse.SC_CREATED, dao.save(currency));
    }

    private Currency getCurrencyFromRequest(HttpServletRequest req) {
        return new Currency(validateParameter(req, "code"),
                validateParameter(req, "name"),
                validateParameter(req, "sign"));
    }

    private String validateParameter(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        if (value == null || value.isBlank()) {
            throw new MissingParameterException("Missing or empty field: " + name);
        }
        return value;
    }
}
