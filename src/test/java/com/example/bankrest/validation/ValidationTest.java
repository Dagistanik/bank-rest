package com.example.bankrest.validation;

import com.example.bankrest.dto.CreateCardRequest;
import com.example.bankrest.dto.LoginRequest;
import com.example.bankrest.dto.SignUpRequest;
import com.example.bankrest.dto.TransferRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ValidationTest {

    @Autowired
    private Validator validator;

    @Test
    void loginRequest_ValidData_NoViolations() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("Test123");

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void loginRequest_EmptyUsername_HasViolation() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword("Test123");

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @Test
    void loginRequest_NullPassword_HasViolation() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword(null);

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void signUpRequest_ValidData_NoViolations() {
        // Arrange
        SignUpRequest request = new SignUpRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("Test123");

        // Act
        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void signUpRequest_InvalidEmail_HasViolation() {
        // Arrange
        SignUpRequest request = new SignUpRequest();
        request.setUsername("testuser");
        request.setEmail("invalid-email");
        request.setPassword("Test123");

        // Act
        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void signUpRequest_WeakPassword_HasViolation() {
        // Arrange
        SignUpRequest request = new SignUpRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("weak"); // Does not meet requirements

        // Act
        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void createCardRequest_ValidData_NoViolations() {
        // Arrange
        CreateCardRequest request = new CreateCardRequest();
        request.setOwnerId(1L);
        request.setInitialBalance(BigDecimal.valueOf(1000.00));

        // Act
        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void createCardRequest_NullOwnerId_HasViolation() {
        // Arrange
        CreateCardRequest request = new CreateCardRequest();
        request.setOwnerId(null);
        request.setInitialBalance(BigDecimal.valueOf(1000.00));

        // Act
        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("ownerId")));
    }

    @Test
    void createCardRequest_NegativeBalance_HasViolation() {
        // Arrange
        CreateCardRequest request = new CreateCardRequest();
        request.setOwnerId(1L);
        request.setInitialBalance(BigDecimal.valueOf(-100.00));

        // Act
        Set<ConstraintViolation<CreateCardRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("initialBalance")));
    }

    @Test
    void transferRequest_ValidData_NoViolations() {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(100.00));

        // Act
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void transferRequest_NullFromCardId_HasViolation() {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromCardId(null);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(100.00));

        // Act
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("fromCardId")));
    }

    @Test
    void transferRequest_ZeroAmount_HasViolation() {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.ZERO);

        // Act
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
    }

    @Test
    void transferRequest_NegativeAmount_HasViolation() {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1L);
        request.setToCardId(2L);
        request.setAmount(BigDecimal.valueOf(-50.00));

        // Act
        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("amount")));
    }
}
