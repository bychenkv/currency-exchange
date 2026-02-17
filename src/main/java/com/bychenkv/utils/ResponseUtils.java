package com.bychenkv.utils;

import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class ResponseUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    private ResponseUtils() {}

    public static void sendJson(HttpServletResponse resp, int code, Object dto) throws IOException {
        resp.setStatus(code);
        mapper.writeValue(resp.getWriter(), dto);
    }

    public static void sendError(HttpServletResponse resp, int code, String message) throws IOException {
        sendJson(resp, code, Map.of("message", message));
    }
}
