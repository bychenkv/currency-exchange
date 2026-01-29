package com.bychenkv.controller;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BaseServlet extends HttpServlet {
    protected static final String VALID_CURRENCY_CODE_REGEX = "^[A-Z]{3}$";

    private ObjectMapper mapper;

    @Override
    public void init() {
        this.mapper = (ObjectMapper) getServletContext().getAttribute("mapper");
    }

    protected void sendJson(HttpServletResponse resp, int code, Object obj) throws IOException {
        resp.setStatus(code);
        mapper.writeValue(resp.getWriter(), obj);
    }

    protected static String getPathParameter(HttpServletRequest req, String name) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            throw new IllegalArgumentException("Parameter '" + name + "' is missing from path");
        }

        String value = pathInfo.replaceFirst("/", "").trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Parameter '" + name + "' is empty");
        }
        return value;
    }

    protected static String getCurrencyCodeParameter(HttpServletRequest req, String name) throws IOException {
        String code = getRequiredParameter(req, name).toUpperCase();
        if (!code.matches(VALID_CURRENCY_CODE_REGEX)) {
            throw new IllegalArgumentException("Invalid currency code: " + code);
        }
        return code;
    }

    protected static BigDecimal getRateParameter(HttpServletRequest req) throws IOException {
        return getRequiredDecimalParameter(req,
                "rate",
                value -> value.compareTo(BigDecimal.ZERO) > 0,
                name -> "The '" + name + "' value must be positive");
    }

    protected static BigDecimal getAmountParameter(HttpServletRequest req) throws IOException {
        return getRequiredDecimalParameter(req,
                "amount",
                value -> value.compareTo(BigDecimal.ZERO) >= 0,
                name -> "The '" + name + "' value must be non-negative");
    }


    private static BigDecimal getRequiredDecimalParameter(HttpServletRequest req,
                                                          String name,
                                                          Predicate<BigDecimal> validator,
                                                          Function<String, String> errorMessageProvider) throws IOException {
        String rawValue = getRequiredParameter(req, name);
        try {
            BigDecimal value = new BigDecimal(rawValue);
            if (!validator.test(value)) {
                throw new IllegalArgumentException(errorMessageProvider.apply(name));
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The '" + name + "' value must be a floating point number");
        }
    }

    protected static String getRequiredParameter(HttpServletRequest req, String name) throws IOException {
        String method = req.getMethod();

        if (method.equals("GET") || method.equals("POST")) {
            return ensureParameter(name, req::getParameter);
        }

        // For some reason, req.getParameter(name) always returns null for PATCH/PUT requests
        // So we need to parse reqeust body manually.
        if (method.equals("PATCH") || method.equals("PUT")) {
            Map<String, String> params = parseRequestBody(req);
            return ensureParameter(name, params::get);
        }

        throw new IllegalArgumentException("Unsupported HTTP method: " + method);
    }

    private static Map<String, String> parseRequestBody(HttpServletRequest req) throws IOException {
        Map<String, String> params = new HashMap<>();
        BufferedReader reader = req.getReader();
        String requestBody = reader.lines().collect(Collectors.joining());

        if (!requestBody.isBlank()) {
            for (String pair : requestBody.split("&")) {
                String[] keyValue = pair.split("=", 2);
                List<String> decoded = Arrays.stream(keyValue)
                        .map(kv -> URLDecoder.decode(kv, StandardCharsets.UTF_8)).toList();
                params.put(decoded.getFirst(), decoded.getLast());
            }
        }
        return params;
    }

    private static String ensureParameter(String name, Function<String, String> getter) {
        String value = getter.apply(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(buildRequiredParameterMessage(name));
        }
        return value;
    }

    private static String buildRequiredParameterMessage(String name) {
        return "Parameter '" + name + "' is required";
    }
}
