package com.bychenkv.controller;

import com.bychenkv.dto.CurrencyCodePair;
import com.bychenkv.exception.ExchangeRateNotFoundException;
import com.bychenkv.exception.InvalidParameterException;
import com.bychenkv.exception.MissingParameterException;
import com.bychenkv.service.ExchangeService;
import com.bychenkv.utils.CurrencyCodePairParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/exchange")
public class ExchangeServlet extends BaseServlet {
    private ExchangeService exchangeService;

    @Override
    public void init() {
        super.init();
        this.exchangeService = (ExchangeService) getServletContext().getAttribute("exchangeService");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            CurrencyCodePair codePair = CurrencyCodePairParser.parse(req, "from", "to");
            double amount = validateAmount(req);

            sendJson(resp, HttpServletResponse.SC_OK, exchangeService.exchange(codePair, amount));

        } catch (InvalidParameterException | MissingParameterException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (ExchangeRateNotFoundException e) {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (SQLException e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private double validateAmount(HttpServletRequest req) throws MissingParameterException,
                                                                 InvalidParameterException {
        String rawAmount = req.getParameter("amount");
        if (rawAmount == null || rawAmount.isBlank()) {
            throw new MissingParameterException("amount");
        }

        try {
            double amount = Double.parseDouble(rawAmount);
            if (amount < 0) {
                throw new InvalidParameterException("Amount must be non negative");
            }
            return amount;
        } catch (NumberFormatException e) {
            throw new InvalidParameterException("Amount must be numeric");
        }
    }
}
