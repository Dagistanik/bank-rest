package com.example.bankrest.exception;

import com.example.bankrest.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Handle validation errors (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                "Validation Failed",
                "Input validation failed",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );

        List<ErrorResponse.ValidationError> validationErrors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            Object rejectedValue = ((FieldError) error).getRejectedValue();
            String errorMessage = error.getDefaultMessage();
            validationErrors.add(new ErrorResponse.ValidationError(fieldName, rejectedValue, errorMessage));
        });

        errorResponse.setValidationErrors(validationErrors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Handle authentication errors
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(BadCredentialsException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                "Authentication Failed",
                "Invalid credentials provided",
                HttpStatus.UNAUTHORIZED.value(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // Handle authorization errors (access)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                "Access Denied",
                "You don't have permission to access this resource",
                HttpStatus.FORBIDDEN.value(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // Handle business logic errors
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleBusinessLogicException(
            RuntimeException ex, HttpServletRequest request) {

        HttpStatus status = determineStatusFromMessage(ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "Business Logic Error",
                ex.getMessage(),
                status.value(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    // Handle "Not Found" errors
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(
            IllegalArgumentException ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                "Invalid Argument",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Handle all other errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(
                "Internal Server Error",
                "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Determine HTTP status based on error message
    private HttpStatus determineStatusFromMessage(String message) {
        if (message == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        String lowerMessage = message.toLowerCase();

        if (lowerMessage.contains("not found") || lowerMessage.contains("does not exist")) {
            return HttpStatus.NOT_FOUND;
        }
        if (lowerMessage.contains("already exists") || lowerMessage.contains("already taken")) {
            return HttpStatus.CONFLICT;
        }
        if (lowerMessage.contains("insufficient") || lowerMessage.contains("invalid") ||
            lowerMessage.contains("cannot") || lowerMessage.contains("blocked")) {
            return HttpStatus.BAD_REQUEST;
        }
        if (lowerMessage.contains("access denied") || lowerMessage.contains("permission")) {
            return HttpStatus.FORBIDDEN;
        }

        return HttpStatus.BAD_REQUEST;
    }
}
