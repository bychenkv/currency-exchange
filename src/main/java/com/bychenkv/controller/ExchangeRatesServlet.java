package com.bychenkv.controller;

import com.bychenkv.dao.ExchangeRateDao;
import com.bychenkv.exception.CurrencyNotFoundException;
import com.bychenkv.exception.InvalidParameterException;
import com.bychenkv.exception.MissingParameterException;
import com.bychenkv.dto.CurrencyCodePair;
import com.bychenkv.model.ExchangeRate;
import com.bychenkv.utils.CurrencyCodePairParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private ExchangeRateDao dao;
    private ObjectMapper mapper;

    @Override
    public void init() {
        this.dao = (ExchangeRateDao) getServletContext().getAttribute("exchangeRateDao");
        this.mapper = (ObjectMapper) getServletContext().getAttribute("mapper");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<ExchangeRate> exchangeRates = dao.findAll();

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.setStatus(HttpServletResponse.SC_OK);

            mapper.writeValue(resp.getWriter(), exchangeRates);
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            CurrencyCodePair codePair = CurrencyCodePairParser.parse(
                    req,
                    "baseCurrencyCode",
                    "targetCurrencyCode"
            );
            double rate = validateExchangeRate(req);

            ExchangeRate exchangeRate = dao.save(codePair, rate);

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.setStatus(HttpServletResponse.SC_CREATED);

            mapper.writeValue(resp.getWriter(), exchangeRate);

        } catch (InvalidParameterException | MissingParameterException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (CurrencyNotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (SQLiteException e) {
            if (e.getResultCode() == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE) {
                resp.sendError(HttpServletResponse.SC_CONFLICT, "Exchange rate already exists");
            }
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
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
