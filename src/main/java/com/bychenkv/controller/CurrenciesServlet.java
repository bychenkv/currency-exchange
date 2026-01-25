package com.bychenkv.controller;

import com.bychenkv.dao.CurrencyDao;
import com.bychenkv.exception.MissingParameterException;
import com.bychenkv.model.Currency;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {
    private CurrencyDao dao;
    private ObjectMapper mapper;

    @Override
    public void init() {
        this.dao = (CurrencyDao) getServletContext().getAttribute("currencyDao");
        this.mapper = (ObjectMapper) getServletContext().getAttribute("mapper");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<Currency> currencies = dao.findAll();

            resp.setStatus(HttpServletResponse.SC_OK);
            mapper.writeValue(resp.getWriter(), currencies);

        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Currency currency = dao.save(getCurrencyFromRequest(req));

            resp.setStatus(HttpServletResponse.SC_CREATED);
            mapper.writeValue(resp.getWriter(), currency);

        } catch (MissingParameterException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLiteException e) {
            if (e.getResultCode() == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE) {
                resp.sendError(HttpServletResponse.SC_CONFLICT, "Currency already exists");
            }
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private Currency getCurrencyFromRequest(HttpServletRequest req) throws MissingParameterException {
        return new Currency(
                validateParameter(req, "code"),
                validateParameter(req, "name"),
                validateParameter(req, "sign")
        );
    }

    private String validateParameter(HttpServletRequest req, String name) throws MissingParameterException {
        String value = req.getParameter(name);

        if (value == null || value.isBlank()) {
            throw new MissingParameterException("Missing or empty field: " + name);
        }

        return value;
    }
}
