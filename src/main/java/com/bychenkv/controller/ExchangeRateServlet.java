package com.bychenkv.controller;

import com.bychenkv.dao.ExchangeRateDao;
import com.bychenkv.dto.CurrencyCodePair;
import com.bychenkv.exception.*;
import com.bychenkv.model.ExchangeRate;
import com.bychenkv.utils.ResponseUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends BaseServlet {
    private ExchangeRateDao dao;

    @Override
    public void init() {
        super.init();
        this.dao = (ExchangeRateDao) getServletContext().getAttribute("exchangeRateDao");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CurrencyCodePair codePair = extractCodePairFromPath(req);
        Optional<ExchangeRate> exchangeRate = dao.findByCodePair(codePair);
        if (exchangeRate.isEmpty()) {
            ResponseUtils.sendError(resp,
                    HttpServletResponse.SC_NOT_FOUND,
                    "Exchange rate for currency pair " + codePair + " not found");
            return;
        }
        sendJson(resp, HttpServletResponse.SC_OK, exchangeRate.get());
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        CurrencyCodePair codePair = extractCodePairFromPath(req);
        double rate = getRateParameter(req);

        ExchangeRate exchangeRate = dao.update(codePair, rate);
        sendJson(resp, HttpServletResponse.SC_OK, exchangeRate);
    }

    private CurrencyCodePair extractCodePairFromPath(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.contentEquals("/")) {
            throw new IllegalArgumentException("Currency pair codes are missing");
        }

        String codePair = pathInfo.replace("/", "").toUpperCase();

        if (!codePair.matches("^[A-Z]{6}$")) {
            throw new IllegalArgumentException("Incorrect code pair format");
        }

        return new CurrencyCodePair(codePair);
    }
}
