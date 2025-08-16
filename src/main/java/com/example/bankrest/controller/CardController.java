package com.example.bankrest.controller;

import com.example.bankrest.dto.CardResponse;
import com.example.bankrest.dto.CreateCardRequest;
import com.example.bankrest.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@Tag(name = "Cards", description = "Bank card management")
@SecurityRequirement(name = "bearerAuth")
public class CardController {

    @Autowired
    private CardService cardService;

    // POST /cards — create card (ADMIN only)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create new card",
        description = "Create new bank card (administrators only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Card created successfully",
            content = @Content(schema = @Schema(implementation = CardResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient access rights")
    })
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CreateCardRequest request) {
        try {
            CardResponse cardResponse = cardService.createCard(request);
            return ResponseEntity.ok(cardResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // GET /cards — search and pagination (ADMIN only)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get card list with pagination",
        description = "Search and paginate cards with filtering options (administrators only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "List of cards"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient access rights")
    })
    public ResponseEntity<Page<CardResponse>> getAllCards(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir,
            @Parameter(description = "Card status filter") @RequestParam(required = false) String status,
            @Parameter(description = "Card owner filter") @RequestParam(required = false) Long ownerId) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<CardResponse> cards = cardService.getAllCardsWithPagination(pageable, status, ownerId);
            return ResponseEntity.ok(cards);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // GET /cards/{id} — view card
    @GetMapping("/{id}")
    @Operation(
        summary = "Get card information",
        description = "View information about specific card"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Card information",
            content = @Content(schema = @Schema(implementation = CardResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<CardResponse> getCard(
            @Parameter(description = "Card ID") @PathVariable Long id) {
        try {
            CardResponse card = cardService.getCardById(id);
            return ResponseEntity.ok(card);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PUT /cards/{id}/block — block card (ADMIN only)
    @PutMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Block card",
        description = "Block card (administrators only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Card blocked successfully",
            content = @Content(schema = @Schema(implementation = CardResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Block error"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient access rights"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<CardResponse> blockCard(
            @Parameter(description = "Card ID") @PathVariable Long id) {
        try {
            CardResponse cardResponse = cardService.blockCard(id);
            return ResponseEntity.ok(cardResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT /cards/{id}/activate — activate card (ADMIN only)
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Activate card",
        description = "Activate card (administrators only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Card activated successfully",
            content = @Content(schema = @Schema(implementation = CardResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Activation error"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient access rights"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<CardResponse> activateCard(
            @Parameter(description = "Card ID") @PathVariable Long id) {
        try {
            CardResponse cardResponse = cardService.activateCard(id);
            return ResponseEntity.ok(cardResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // DELETE /cards/{id} — delete card (ADMIN only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete card",
        description = "Delete card (administrators only)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Card deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Delete error"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient access rights"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Card not found")
    })
    public ResponseEntity<Void> deleteCard(
            @Parameter(description = "Card ID") @PathVariable Long id) {
        try {
            cardService.deleteCard(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // USER - get own cards
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    @Operation(
        summary = "Get user's own cards",
        description = "Get list of cards belonging to current user"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "List of user cards"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient access rights")
    })
    public ResponseEntity<List<CardResponse>> getMyCards() {
        try {
            List<CardResponse> cards = cardService.getCardsByUser();
            return ResponseEntity.ok(cards);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
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
