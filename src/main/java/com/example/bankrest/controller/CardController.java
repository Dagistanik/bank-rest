package com.example.bankrest.controller;

import com.example.bankrest.dto.CardResponse;
import com.example.bankrest.dto.CreateCardRequest;
import com.example.bankrest.service.CardService;
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
public class CardController {

    @Autowired
    private CardService cardService;

    // POST /cards — создание карты (только ADMIN)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CreateCardRequest request) {
        try {
            CardResponse cardResponse = cardService.createCard(request);
            return ResponseEntity.ok(cardResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // GET /cards — поиск и пагинация (только ADMIN)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardResponse>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long ownerId) {
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

    // GET /cards/{id} — просмотр карты
    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> getCard(@PathVariable Long id) {
        try {
            CardResponse card = cardService.getCardById(id);
            return ResponseEntity.ok(card);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // PUT /cards/{id}/block — блокировка карты (только ADMIN)
    @PutMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> blockCard(@PathVariable Long id) {
        try {
            CardResponse cardResponse = cardService.blockCard(id);
            return ResponseEntity.ok(cardResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT /cards/{id}/activate — активация карты (только ADMIN)
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> activateCard(@PathVariable Long id) {
        try {
            CardResponse cardResponse = cardService.activateCard(id);
            return ResponseEntity.ok(cardResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // DELETE /cards/{id} — удаление карты (только ADMIN)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        try {
            cardService.deleteCard(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // USER - получение своих карт
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CardResponse>> getMyCards() {
        try {
            List<CardResponse> cards = cardService.getCardsByUser();
            return ResponseEntity.ok(cards);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
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
