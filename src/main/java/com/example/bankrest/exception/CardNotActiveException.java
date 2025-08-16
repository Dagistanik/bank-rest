package com.example.bankrest.exception;

public class CardNotActiveException extends RuntimeException {
    public CardNotActiveException(String message) {
        super(message);
    }

    public CardNotActiveException(String message, Throwable cause) {
        super(message, cause);
    }
}
