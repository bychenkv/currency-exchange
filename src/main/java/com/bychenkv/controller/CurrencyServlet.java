package com.bychenkv.controller;

import com.bychenkv.dao.CurrencyDao;
import com.bychenkv.model.Currency;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {
    private CurrencyDao dao;
    private ObjectMapper mapper;

    @Override
    public void init() {
        this.dao = (CurrencyDao) getServletContext().getAttribute("currencyDao");
        this.mapper = (ObjectMapper) getServletContext().getAttribute("mapper");
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
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Currency " + code + " not found");
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            mapper.writeValue(resp.getWriter(), currency.get());

        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
