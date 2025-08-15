package com.example.bankrest.exception;

public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(String message) {
        super(message);
    }

    public CardNotFoundException(Long cardId) {
        super("Card not found with id: " + cardId);
    }
}
