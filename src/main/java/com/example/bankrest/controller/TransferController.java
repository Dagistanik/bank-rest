package com.example.bankrest.controller;

import com.example.bankrest.dto.TransferRequest;
import com.example.bankrest.service.CardService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfer")
public class TransferController {

    @Autowired
    private CardService cardService;

    // POST /transfer — перевод между своими картами (только USER)
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse> transferBetweenOwnCards(@Valid @RequestBody TransferRequest request) {
        try {
            cardService.transferBetweenOwnCards(
                request.getFromCardId(),
                request.getToCardId(),
                request.getAmount()
            );
            return ResponseEntity.ok(new ApiResponse(true, "Transfer completed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    // Вспомогательный класс для ответов API
    public static class ApiResponse {
        private Boolean success;
        private String message;

        public ApiResponse(Boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public Boolean getSuccess() {
            return success;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
