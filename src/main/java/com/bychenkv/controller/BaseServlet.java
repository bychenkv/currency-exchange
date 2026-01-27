package com.bychenkv.controller;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

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
}
