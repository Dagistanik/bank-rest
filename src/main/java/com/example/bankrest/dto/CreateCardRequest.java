package com.example.bankrest.dto;

import com.example.bankrest.entity.Card;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateCardRequest {

    @NotNull(message = "Owner ID is required")
    @Positive(message = "Owner ID must be positive")
    private Long ownerId;

    @Future(message = "Expiry date must be in the future")
    private LocalDate expiryDate;

    @DecimalMin(value = "0.0", inclusive = true, message = "Initial balance cannot be negative")
    @DecimalMax(value = "999999.99", message = "Initial balance cannot exceed 999,999.99")
    private BigDecimal initialBalance = BigDecimal.ZERO;

    // Конструкторы
    public CreateCardRequest() {}

    public CreateCardRequest(Long ownerId, LocalDate expiryDate, BigDecimal initialBalance) {
        this.ownerId = ownerId;
        this.expiryDate = expiryDate;
        this.initialBalance = initialBalance;
    }

    // Геттеры и сеттеры
    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }
}
