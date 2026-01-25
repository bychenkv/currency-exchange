package com.bychenkv.controller;

import com.bychenkv.dao.CurrencyDao;
import com.bychenkv.exception.MissingParameterException;
import com.bychenkv.model.Currency;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;

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
        try {
            sendJson(resp, HttpServletResponse.SC_OK, dao.findAll());
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Currency currency = getCurrencyFromRequest(req);
            sendJson(resp, HttpServletResponse.SC_CREATED, dao.save(currency));
        } catch (MissingParameterException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLiteException e) {
            if (e.getResultCode() == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE) {
                sendError(resp, HttpServletResponse.SC_CONFLICT, "Currency already exists");
            }
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private Currency getCurrencyFromRequest(HttpServletRequest req) throws MissingParameterException {
        return new Currency(validateParameter(req, "code"),
                validateParameter(req, "name"),
                validateParameter(req, "sign"));
    }

    private String validateParameter(HttpServletRequest req, String name) throws MissingParameterException {
        String value = req.getParameter(name);
        if (value == null || value.isBlank()) {
            throw new MissingParameterException("Missing or empty field: " + name);
        }
        return value;
    }
}
