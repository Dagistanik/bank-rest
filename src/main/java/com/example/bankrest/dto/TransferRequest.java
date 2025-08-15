package com.example.bankrest.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class TransferRequest {

    @NotNull(message = "Source card ID is required")
    @Positive(message = "Source card ID must be positive")
    private Long fromCardId;

    @NotNull(message = "Destination card ID is required")
    @Positive(message = "Destination card ID must be positive")
    private Long toCardId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Amount cannot exceed 999,999.99")
    @Digits(integer = 6, fraction = 2, message = "Amount must have at most 6 integer digits and 2 decimal places")
    private BigDecimal amount;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    // Конструкторы
    public TransferRequest() {}

    public TransferRequest(Long fromCardId, Long toCardId, BigDecimal amount, String description) {
        this.fromCardId = fromCardId;
        this.toCardId = toCardId;
        this.amount = amount;
        this.description = description;
    }

    // Геттеры и сеттеры
    public Long getFromCardId() {
        return fromCardId;
    }

    public void setFromCardId(Long fromCardId) {
        this.fromCardId = fromCardId;
    }

    public Long getToCardId() {
        return toCardId;
    }

    public void setToCardId(Long toCardId) {
        this.toCardId = toCardId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @AssertTrue(message = "Source and destination cards must be different")
    public boolean isValidTransfer() {
        return fromCardId == null || toCardId == null || !fromCardId.equals(toCardId);
    }
}
