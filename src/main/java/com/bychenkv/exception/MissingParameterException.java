package com.bychenkv.exception;

public class MissingParameterException extends Exception {
    public MissingParameterException(String paramName) {
        super("Missing or empty field: " + paramName);
    }
}
