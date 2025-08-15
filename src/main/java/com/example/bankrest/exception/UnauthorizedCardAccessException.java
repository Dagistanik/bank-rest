package com.example.bankrest.exception;

public class UnauthorizedCardAccessException extends RuntimeException {
    public UnauthorizedCardAccessException(String message) {
        super(message);
    }

    public UnauthorizedCardAccessException() {
        super("You can only access your own cards");
    }
}
