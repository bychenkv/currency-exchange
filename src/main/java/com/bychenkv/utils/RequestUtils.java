package com.bychenkv.utils;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class RequestUtils {
    private RequestUtils() {}

    public static String getPathParameter(HttpServletRequest req, String name) {
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

    public static Map<String, String> parseRequestBody(HttpServletRequest req) throws IOException {
        Map<String, String> params = new HashMap<>();
        String body = req.getReader().lines().collect(Collectors.joining());

        if (!body.isBlank()) {
            for (String pair : body.split("&")) {
                String[] keyValue = pair.split("=", 2);
                String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                String value = keyValue.length > 1
                        ? URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8)
                        : "";
                params.put(key, value);
            }
        }

        return params;
    }

    public static Map<String, String> getQueryParams(HttpServletRequest req) {
        return req.getParameterMap().entrySet().stream()
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue()[0]
                        )
                );
    }
}
