package com.bychenkv.controller;

import com.bychenkv.dao.CurrencyDao;
import com.bychenkv.model.Currency;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/currency/*")
public class CurrencyServlet extends BaseServlet {
    private CurrencyDao dao;

    @Override
    public void init() {
        super.init();
        this.dao = (CurrencyDao) getServletContext().getAttribute("currencyDao");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.contentEquals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Currency code is missing");
            return;
        }
        String code = pathInfo.replace("/", "").toUpperCase();

        try {
            Optional<Currency> currency = dao.findByCode(code);
            if (currency.isEmpty()) {
                // TODO: maybe use CurrencyNotFoundException
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Currency " + code + " not found");
                return;
            }

            sendJson(resp, HttpServletResponse.SC_OK, currency.get());

        } catch (SQLException e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
