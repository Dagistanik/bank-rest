package com.example.bankrest.controller;

import com.example.bankrest.dto.CardResponse;
import com.example.bankrest.dto.CreateCardRequest;
import com.example.bankrest.entity.Card;
import com.example.bankrest.service.CardService;
import com.example.bankrest.util.JwtTokenProvider;
import com.example.bankrest.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateCardRequest createCardRequest;
    private CardResponse cardResponse;

    @BeforeEach
    void setUp() {
        createCardRequest = new CreateCardRequest();
        createCardRequest.setOwnerId(1L);
        createCardRequest.setInitialBalance(BigDecimal.valueOf(1000.00));

        cardResponse = new CardResponse();
        cardResponse.setId(1L);
        cardResponse.setBalance(BigDecimal.valueOf(1000.00));
        cardResponse.setStatus(Card.CardStatus.ACTIVE);
        cardResponse.setOwnerUsername("testuser");
        cardResponse.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_Success() throws Exception {
        // Arrange
        when(cardService.createCard(any(CreateCardRequest.class))).thenReturn(cardResponse);

        // Act & Assert
        mockMvc.perform(post("/api/cards")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCardRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andExpect(jsonPath("$.ownerUsername").value("testuser"));

        verify(cardService).createCard(any(CreateCardRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCard_Forbidden_UserRole() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/cards")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCardRequest)))
                .andExpect(status().isForbidden());

        verify(cardService, never()).createCard(any(CreateCardRequest.class));
    }

    @Test
    void createCard_Unauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/cards")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCardRequest)))
                .andExpect(status().isUnauthorized());

        verify(cardService, never()).createCard(any(CreateCardRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_ValidationError() throws Exception {
        // Arrange
        createCardRequest.setOwnerId(null); // Invalid data

        // Act & Assert
        mockMvc.perform(post("/api/cards")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCardRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_ServiceError() throws Exception {
        // Arrange
        when(cardService.createCard(any(CreateCardRequest.class)))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(post("/api/cards")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCardRequest)))
                .andExpect(status().isBadRequest());

        verify(cardService).createCard(any(CreateCardRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCards_Success() throws Exception {
        // Arrange
        List<CardResponse> cards = List.of(cardResponse);
        Page<CardResponse> cardPage = new PageImpl<>(cards, PageRequest.of(0, 10), 1);
        when(cardService.getAllCardsWithPagination(any(), any(), any())).thenReturn(cardPage);

        // Act & Assert
        mockMvc.perform(get("/api/cards")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "id")
                .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(cardService).getAllCardsWithPagination(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllCards_Forbidden_UserRole() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isForbidden());

        verify(cardService, never()).getAllCardsWithPagination(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCard_Success() throws Exception {
        // Arrange
        when(cardService.getCardById(1L)).thenReturn(cardResponse);

        // Act & Assert
        mockMvc.perform(get("/api/cards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.balance").value(1000.00));

        verify(cardService).getCardById(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCard_NotFound() throws Exception {
        // Arrange
        when(cardService.getCardById(1L)).thenThrow(new RuntimeException("Card not found"));

        // Act & Assert - Controller returns 404 for exceptions
        mockMvc.perform(get("/api/cards/1"))
                .andExpect(status().isNotFound());

        verify(cardService).getCardById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockCard_Success() throws Exception {
        // Arrange
        cardResponse.setStatus(Card.CardStatus.BLOCKED);
        when(cardService.blockCard(1L)).thenReturn(cardResponse);

        // Act & Assert
        mockMvc.perform(put("/api/cards/1/block")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(cardService).blockCard(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockCard_ServiceError() throws Exception {
        // Arrange
        when(cardService.blockCard(1L)).thenThrow(new RuntimeException("Card not found"));

        // Act & Assert - Controller returns 400 for exceptions
        mockMvc.perform(put("/api/cards/1/block")
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cardService).blockCard(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void blockCard_Forbidden_UserRole() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/cards/1/block")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(cardService, never()).blockCard(anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateCard_Success() throws Exception {
        // Arrange
        cardResponse.setStatus(Card.CardStatus.ACTIVE);
        when(cardService.activateCard(1L)).thenReturn(cardResponse);

        // Act & Assert
        mockMvc.perform(put("/api/cards/1/activate")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(cardService).activateCard(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_Success() throws Exception {
        // Arrange
        doNothing().when(cardService).deleteCard(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/cards/1")
                .with(csrf()))
                .andExpect(status().isOk());

        verify(cardService).deleteCard(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteCard_Forbidden_UserRole() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/cards/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(cardService, never()).deleteCard(anyLong());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_NotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Card not found")).when(cardService).deleteCard(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/cards/1")
                .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(cardService).deleteCard(1L);
    }
}
