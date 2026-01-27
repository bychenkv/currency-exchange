package com.bychenkv.controller;

import com.bychenkv.dao.CurrencyDao;
import com.bychenkv.model.Currency;
import com.bychenkv.utils.ResponseUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
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
            ResponseUtils.sendError(resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Currency code is missing");
            return;
        }
        String code = pathInfo.replace("/", "").toUpperCase();

        Optional<Currency> currency = dao.findByCode(code);
        if (currency.isEmpty()) {
            ResponseUtils.sendError(resp,
                    HttpServletResponse.SC_NOT_FOUND,
                    "Currency " + code + " not found");
            return;
        }
        sendJson(resp, HttpServletResponse.SC_OK, currency.get());
    }
}
