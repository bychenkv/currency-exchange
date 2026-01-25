package com.bychenkv.controller;

import com.bychenkv.dao.ExchangeRateDao;
import com.bychenkv.exception.CurrencyNotFoundException;
import com.bychenkv.exception.InvalidParameterException;
import com.bychenkv.exception.MissingParameterException;
import com.bychenkv.dto.CurrencyCodePair;
import com.bychenkv.utils.CurrencyCodePairParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;

import java.io.IOException;
import java.sql.SQLException;

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
        try {
            sendJson(resp, HttpServletResponse.SC_OK, dao.findAll());
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            CurrencyCodePair codePair = CurrencyCodePairParser.parse(req,
                    "baseCurrencyCode",
                    "targetCurrencyCode");
            double rate = validateExchangeRate(req);

            sendJson(resp, HttpServletResponse.SC_CREATED, dao.save(codePair, rate));

        } catch (InvalidParameterException | MissingParameterException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (CurrencyNotFoundException e) {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (SQLiteException e) {
            if (e.getResultCode() == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE) {
                sendError(resp, HttpServletResponse.SC_CONFLICT, "Exchange rate already exists");
            }
        } catch (SQLException e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private double validateExchangeRate(HttpServletRequest req) throws MissingParameterException,
                                                                       InvalidParameterException {
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
