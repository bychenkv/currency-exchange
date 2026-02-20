package com.bychenkv.utils;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import static com.bychenkv.utils.RequestUtils.getParameterMap;
import static com.bychenkv.utils.RequestUtils.parseRequestBody;

public class RequestParams {
    private final Map<String, String> params;

    public RequestParams(Map<String, String> params) {
        this.params = params;
    }

    public static RequestParams from(HttpServletRequest req) throws IOException {
        String method = req.getMethod().toUpperCase();

        Map<String, String> params = switch (method) {
            case "GET", "POST" -> getParameterMap(req);
            case "PUT", "PATCH" -> parseRequestBody(req);
            default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        };

        return new RequestParams(params);
    }

    public String requireRaw(String name) {
        String value = params.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Parameter '" + name + "' is required");
        }
        return value;
    }

    public BigDecimal requireDecimal(String name) {
        String rawValue = requireRaw(name);
        try {
            return new BigDecimal(rawValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Parameter '" + name + "' must be a number");
        }
    }
}
