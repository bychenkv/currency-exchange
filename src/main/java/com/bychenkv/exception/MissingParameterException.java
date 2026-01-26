package com.bychenkv.exception;

public class MissingParameterException extends RuntimeException {
    public MissingParameterException(String paramName) {
        super("Missing or empty field: " + paramName);
    }
}
