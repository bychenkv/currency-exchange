package com.bychenkv.controller;

import com.bychenkv.dao.ExchangeRateDao;
import com.bychenkv.exception.*;
import com.bychenkv.dto.CurrencyCodePair;
import com.bychenkv.model.ExchangeRate;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
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
            CurrencyCodePair codePair = extractCodePairFromPath(req);

            Optional<ExchangeRate> exchangeRate = dao.findByCodePair(codePair);
            if (exchangeRate.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Exchange rate for currency pair " + codePair + " not found");
                return;
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            mapper.writeValue(resp.getWriter(), exchangeRate.get());

        } catch (InvalidCodePair e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            CurrencyCodePair codePair = extractCodePairFromPath(req);
            double rate = validateExchangeRate(req);

            ExchangeRate exchangeRate = dao.update(codePair, rate);

            resp.setStatus(HttpServletResponse.SC_OK);
            mapper.writeValue(resp.getWriter(), exchangeRate);

        } catch (InvalidCodePair | MissingParameterException | InvalidParameterException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (CurrencyNotFoundException | ExchangeRateNotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (SQLException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private CurrencyCodePair extractCodePairFromPath(HttpServletRequest req) throws InvalidCodePair {
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.contentEquals("/")) {
            throw new InvalidCodePair("Currency pair codes are missing");
        }

        String codePair = pathInfo.replace("/", "").toUpperCase();

        if (!codePair.matches("^[A-Z]{6}$")) {
            throw new InvalidCodePair("Incorrect code pair format");
        }

        return new CurrencyCodePair(codePair);
    }

    private double validateExchangeRate(HttpServletRequest req) throws MissingParameterException,
                                                                       InvalidParameterException,
                                                                       IOException {
        String rawRate = getRateParameter(req);
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

    private String getRateParameter(HttpServletRequest req) throws IOException, MissingParameterException {
        return req.getReader().lines()
                .map(parts -> URLDecoder.decode(parts, StandardCharsets.UTF_8))
                .flatMap(l -> Arrays.stream(l.split("&")))
                .map(kv -> kv.split("=", 2))
                .filter(parts -> parts.length == 2 && parts[0].contentEquals("rate"))
                .map(parts -> parts[1])
                .findFirst()
                .orElseThrow(() -> new MissingParameterException("rate"));
    }
}
