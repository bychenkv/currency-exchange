package com.bychenkv.filter;

import com.bychenkv.exception.*;
import com.bychenkv.utils.ResponseUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/*")
public class ExceptionHandleFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException {
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            handleException((HttpServletResponse) response, (HttpServletRequest) request, e);
        }
    }

    private void handleException(HttpServletResponse resp,
                                 HttpServletRequest req,
                                 Exception e) throws IOException {
        int code = switch (e) {
            case CurrencyAlreadyExistsException _,
                 ExchangeRateAlreadyExistsException _ -> HttpServletResponse.SC_CONFLICT;

            case CurrencyNotFoundException _,
                 ExchangeRateNotFoundException _ -> HttpServletResponse.SC_NOT_FOUND;

            case IllegalArgumentException _ -> HttpServletResponse.SC_BAD_REQUEST;

            default -> {
                req.getServletContext().log("Error occurred: ", e);
                yield HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }
        };

        ResponseUtils.sendError(resp, code, e.getMessage());
    }
}
