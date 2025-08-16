package com.example.bankrest.controller;

import com.example.bankrest.dto.TransferRequest;
import com.example.bankrest.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfer")
@Tag(name = "Transfers", description = "Transfers between cards")
@SecurityRequirement(name = "bearerAuth")
public class TransferController {

    @Autowired
    private CardService cardService;

    // POST /transfer — transfer between own cards (USER only)
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(
        summary = "Transfer between cards",
        description = "Transfer funds between own cards (users only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Transfer completed successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Transfer error",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient access rights")
    })
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

    // Helper class for API responses
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
