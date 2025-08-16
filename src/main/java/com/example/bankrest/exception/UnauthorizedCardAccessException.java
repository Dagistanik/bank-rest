package com.example.bankrest.exception;

public class UnauthorizedCardAccessException extends RuntimeException {
    public UnauthorizedCardAccessException(String message) {
        super(message);
    }

    public UnauthorizedCardAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
