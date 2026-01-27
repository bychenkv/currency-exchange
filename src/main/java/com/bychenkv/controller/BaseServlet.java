package com.bychenkv.controller;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

public class BaseServlet extends HttpServlet {
    private ObjectMapper mapper;

    @Override
    public void init() {
        this.mapper = (ObjectMapper) getServletContext().getAttribute("mapper");
    }

    protected void sendJson(HttpServletResponse resp, int code, Object obj) throws IOException {
        resp.setStatus(code);
        mapper.writeValue(resp.getWriter(), obj);
    }

    protected static String getCurrencyCodeParameter(HttpServletRequest req, String name) throws IOException {
        String code = getRequiredParameter(req, name);
        code = code.toUpperCase();
        if (!code.matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("Invalid currency code: " + code +
                                               ". It must comply with the ISO 4217 format.");
        }
        return code;
    }

    protected static double getRateParameter(HttpServletRequest req) throws IOException {
        return getRequiredDoubleParameter(req,
                "rate",
                value -> value > 0,
                name -> "The '" + name + "' value must be positive");
    }

    protected static double getAmountParameter(HttpServletRequest req) throws IOException {
        return getRequiredDoubleParameter(req,
                "amount",
                value -> value >= 0,
                name -> "The '" + name + "' value must be non-negative");
    }


    protected static String getRequiredParameter(HttpServletRequest req, String name) throws IOException {
        String method = req.getMethod();

        if (method.equals("GET") || method.equals("POST")) {
            String value = req.getParameter(name);
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(buildRequiredParameterMessage(name));
            }
            return value;
        }

        if (method.equals("PATCH") || method.equals("PUT")) {
            return req.getReader().lines()
                    .map(parts -> URLDecoder.decode(parts, StandardCharsets.UTF_8))
                    .flatMap(l -> Arrays.stream(l.split("&")))
                    .map(kv -> kv.split("=", 2))
                    .filter(parts -> parts.length == 2 && parts[0].equals(name))
                    .map(parts -> parts[1])
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(buildRequiredParameterMessage(name)));
        }

        throw new IllegalArgumentException("Unsupported HTTP method: " + method);
    }

    private static double getRequiredDoubleParameter(HttpServletRequest req,
                                              String name,
                                              Predicate<Double> validator,
                                              Function<String, String> errorMessageProvider) throws IOException {
        String rawValue = getRequiredParameter(req, name);
        try {
            double value = Double.parseDouble(rawValue);
            if (!validator.test(value)) {
                throw new IllegalArgumentException(errorMessageProvider.apply(name));
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The '" + name + "' value must be a floating point number");
        }
    }

    private static String buildRequiredParameterMessage(String name) {
        return "Parameter '" + name + "' is required";
    }
}
