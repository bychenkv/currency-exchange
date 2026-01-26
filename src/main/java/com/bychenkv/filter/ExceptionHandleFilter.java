package com.bychenkv.filter;

import com.bychenkv.exception.*;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/*")
public class ExceptionHandleFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException {
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            handleException((HttpServletResponse) response, e);
        }
    }

    private void handleException(HttpServletResponse resp, Exception e) throws IOException {
        int code;
        if (e instanceof CurrencyAlreadyExistsException || e instanceof ExchangeRateAlreadyExistsException) {
            code = HttpServletResponse.SC_CONFLICT;
        } else if (e instanceof CurrencyNotFoundException || e instanceof ExchangeRateNotFoundException) {
            code = HttpServletResponse.SC_NOT_FOUND;
        } else if (e instanceof InvalidParameterException || e instanceof MissingParameterException) {
            code = HttpServletResponse.SC_BAD_REQUEST;
        } else {
            code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }

        sendError(resp, code, e.getMessage());
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.getWriter().write("{\"message\": \"" + message + "\"}");
    }
}
