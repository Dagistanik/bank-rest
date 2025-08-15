package com.example.bankrest.exception;

public class CardNotActiveException extends RuntimeException {
    public CardNotActiveException(String message) {
        super(message);
    }

    public CardNotActiveException(Long cardId) {
        super("Card with id " + cardId + " is not active");
    }
}
