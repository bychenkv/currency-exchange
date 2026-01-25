package com.bychenkv.utils;

import com.bychenkv.dto.CurrencyCodePair;
import com.bychenkv.exception.InvalidParameterException;
import com.bychenkv.exception.MissingParameterException;
import jakarta.servlet.http.HttpServletRequest;

public class CurrencyCodePairParser {
    public static CurrencyCodePair parse(HttpServletRequest req,
                                         String baseName,
                                         String targetName) throws InvalidParameterException,
                                                                   MissingParameterException {
            return new CurrencyCodePair(
                    validateCurrencyCode(req, baseName),
                    validateCurrencyCode(req, targetName)
            );
    }

    private static String validateCurrencyCode(HttpServletRequest req,
                                               String paramName) throws MissingParameterException,
                                                                        InvalidParameterException {
        String code = req.getParameter(paramName);
        if (code == null || code.isBlank()) {
            throw new MissingParameterException(paramName);
        }

        if (!code.matches("^[a-zA-Z]{3}$")) {
            throw new InvalidParameterException("Invalid currency code: " + code +
                                                ". It must comply with the ISO 4217 format.");
        }
        return code.toUpperCase();
    }
}
