package com.bychenkv.utils;

import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ResponseUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void sendError(HttpServletResponse resp, int code, String message) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8);
        resp.setStatus(code);
        mapper.writeValue(resp.getWriter(), Map.of("message", message));
    }
}
