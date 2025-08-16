package com.example.bankrest.controller;

import com.example.bankrest.dto.TransferRequest;
import com.example.bankrest.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TransferControllerSimpleTest {

    @Mock
    private CardService cardService;

    @InjectMocks
    private TransferController transferController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transferController).build();
        objectMapper = new ObjectMapper();

        transferRequest = new TransferRequest();
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardId(2L);
        transferRequest.setAmount(BigDecimal.valueOf(100.00));
    }

    @Test
    @WithMockUser(roles = "USER")
    void transferBetweenOwnCards_Success() throws Exception {
        // Arrange
        doNothing().when(cardService).transferBetweenOwnCards(1L, 2L, BigDecimal.valueOf(100.00));

        // Act & Assert
        mockMvc.perform(post("/api/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Transfer completed successfully"));

        verify(cardService).transferBetweenOwnCards(1L, 2L, BigDecimal.valueOf(100.00));
    }

    @Test
    void transferBetweenOwnCards_ServiceError() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Insufficient balance"))
                .when(cardService).transferBetweenOwnCards(1L, 2L, BigDecimal.valueOf(100.00));

        // Act & Assert
        mockMvc.perform(post("/api/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Insufficient balance"));

        verify(cardService).transferBetweenOwnCards(1L, 2L, BigDecimal.valueOf(100.00));
    }
}
