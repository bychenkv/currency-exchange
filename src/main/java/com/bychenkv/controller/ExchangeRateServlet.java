package com.bychenkv.controller;

import com.bychenkv.dao.ExchangeRateDao;
import com.bychenkv.model.ExchangeRate;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private final ExchangeRateDao dao = new ExchangeRateDao();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.contentEquals("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Currency pair codes are missing");
            return;
        }

        String codePair = pathInfo.replace("/", "").toUpperCase();

        if (!codePair.matches("^[A-Z]{6}$")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect code pair format");
            return;
        }

        String baseCode = codePair.substring(0, 3);
        String targetCode = codePair.substring(3, 6);

        try {
            Optional<ExchangeRate> exchangeRate = dao.findByCodePair(baseCode, targetCode);

            if (exchangeRate.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Exchange rate for currency pair " + codePair + " not found");
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            mapper.writeValue(resp.getWriter(), exchangeRate.get());

        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
