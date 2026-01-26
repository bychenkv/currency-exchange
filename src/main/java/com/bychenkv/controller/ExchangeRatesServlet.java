package com.bychenkv.controller;

import com.bychenkv.dao.ExchangeRateDao;
import com.bychenkv.dto.CurrencyCodePair;
import com.bychenkv.exception.InvalidParameterException;
import com.bychenkv.exception.MissingParameterException;
import com.bychenkv.utils.CurrencyCodePairParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

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
        sendJson(resp, HttpServletResponse.SC_OK, dao.findAll());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CurrencyCodePair codePair = CurrencyCodePairParser.parse(req,
                    "baseCurrencyCode",
                    "targetCurrencyCode");
        double rate = validateExchangeRate(req);

        sendJson(resp, HttpServletResponse.SC_CREATED, dao.save(codePair, rate));
    }

    private double validateExchangeRate(HttpServletRequest req) {
        String rawRate = req.getParameter("rate");
        if (rawRate == null || rawRate.isBlank()) {
            throw new MissingParameterException("rate");
        }

        try {
            double rate = Double.parseDouble(rawRate);
            if (rate <= 0) {
                throw new InvalidParameterException("Exchange rate must be positive");
            }
            return rate;
        } catch (NumberFormatException e) {
            throw new InvalidParameterException("Exchange rate must be numeric");
        }
    }
}
