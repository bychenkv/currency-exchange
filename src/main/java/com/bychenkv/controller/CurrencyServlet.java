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
        String code = getPathParameter(req, "code").toUpperCase();
        if (!code.matches(VALID_CURRENCY_CODE_REGEX)) {
            ResponseUtils.sendError(resp,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid currency code: " + code);
        }

        Optional<Currency> currency = dao.findByCode(code);
        if (currency.isEmpty()) {
            ResponseUtils.sendError(resp,
                    HttpServletResponse.SC_NOT_FOUND,
                    "Currency not found: " + code);
            return;
        }
        sendJson(resp, HttpServletResponse.SC_OK, currency.get());
    }
}
