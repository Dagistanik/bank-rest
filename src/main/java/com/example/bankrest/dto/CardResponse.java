package com.example.bankrest.dto;

import com.example.bankrest.entity.Card;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CardResponse {
    private Long id;
    private String maskedCardNumber;
    private String ownerUsername;
    private LocalDate expiryDate;
    private Card.CardStatus status;
    private BigDecimal balance;
    private LocalDateTime createdAt;

    // Конструкторы
    public CardResponse() {}

    public CardResponse(Long id, String maskedCardNumber, String ownerUsername,
                       LocalDate expiryDate, Card.CardStatus status,
                       BigDecimal balance, LocalDateTime createdAt) {
        this.id = id;
        this.maskedCardNumber = maskedCardNumber;
        this.ownerUsername = ownerUsername;
        this.expiryDate = expiryDate;
        this.status = status;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public Card.CardStatus getStatus() {
        return status;
    }

    public void setStatus(Card.CardStatus status) {
        this.status = status;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
