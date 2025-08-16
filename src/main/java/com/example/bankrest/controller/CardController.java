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
@Tag(name = "Cards", description = "Управление банковскими картами")
@SecurityRequirement(name = "bearerAuth")
public class CardController {

    @Autowired
    private CardService cardService;

    // POST /cards — создание карты (только ADMIN)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Создание новой карты",
        description = "Создание новой банковской карты (только для администраторов)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Карта успешно создана",
            content = @Content(schema = @Schema(implementation = CardResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Ошибка валидации"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Недостаточно прав доступа")
    })
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
    @Operation(
        summary = "Получение списка карт с пагинацией",
        description = "Поиск и пагинация карт с возможностью фильтрации (только для администраторов)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Список карт"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Недостаточно прав доступа")
    })
    public ResponseEntity<Page<CardResponse>> getAllCards(
            @Parameter(description = "Номер страницы") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Поле для сортировки") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Направление сортировки") @RequestParam(defaultValue = "asc") String sortDir,
            @Parameter(description = "Фильтр по статусу карты") @RequestParam(required = false) String status,
            @Parameter(description = "Фильтр по владельцу карты") @RequestParam(required = false) Long ownerId) {
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
    @Operation(
        summary = "Получение информации о карте",
        description = "Просмотр информации о конкретной карте"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Информация о карте",
            content = @Content(schema = @Schema(implementation = CardResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    public ResponseEntity<CardResponse> getCard(
            @Parameter(description = "ID карты") @PathVariable Long id) {
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
    @Operation(
        summary = "Блокировка карты",
        description = "Блокировка карты (только для администраторов)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Карта успешно заблокирована",
            content = @Content(schema = @Schema(implementation = CardResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Ошибка при блокировке"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Недостаточно прав доступа"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    public ResponseEntity<CardResponse> blockCard(
            @Parameter(description = "ID карты") @PathVariable Long id) {
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
    @Operation(
        summary = "Активация карты",
        description = "Активация карты (только для администраторов)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Карта успешно активирована",
            content = @Content(schema = @Schema(implementation = CardResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Ошибка при активации"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Недостаточно прав доступа"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    public ResponseEntity<CardResponse> activateCard(
            @Parameter(description = "ID карты") @PathVariable Long id) {
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
    @Operation(
        summary = "Удаление карты",
        description = "Удаление карты (только для администраторов)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Карта успешно удалена"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Ошибка при удалении"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Недостаточно прав доступа"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Карта не найдена")
    })
    public ResponseEntity<Void> deleteCard(
            @Parameter(description = "ID карты") @PathVariable Long id) {
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
