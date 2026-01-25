package com.bychenkv.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebFilter("/*")
public class ResponseHeadersFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req,
                         ServletResponse resp,
                         FilterChain chain) throws IOException, ServletException {
        resp.setCharacterEncoding(StandardCharsets.UTF_8);
        resp.setContentType("application/json");
        chain.doFilter(req, resp);
    }
}
