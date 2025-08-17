package com.example.bankrest.integration;

import com.example.bankrest.dto.CreateCardRequest;
import com.example.bankrest.dto.LoginRequest;
import com.example.bankrest.dto.SignUpRequest;
import com.example.bankrest.dto.TransferRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class BankingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // Register user
        SignUpRequest userRequest = new SignUpRequest();
        userRequest.setUsername("testuser");
        userRequest.setEmail("user@test.com");
        userRequest.setPassword("User123");

        MvcResult userResult = mockMvc.perform(post("/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Extract user token
        String userResponse = userResult.getResponse().getContentAsString();
        userToken = extractTokenFromResponse(userResponse);

        // Register administrator
        SignUpRequest adminRequest = new SignUpRequest();
        adminRequest.setUsername("admin");
        adminRequest.setEmail("admin@test.com");
        adminRequest.setPassword("Admin123");

        MvcResult adminResult = mockMvc.perform(post("/auth/register-admin")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Extract administrator token
        String adminResponse = adminResult.getResponse().getContentAsString();
        adminToken = extractTokenFromResponse(adminResponse);
    }

    @Test
    void fullBankingWorkflow_Success() throws Exception {
        // 1. Create card as administrator
        CreateCardRequest cardRequest = new CreateCardRequest();
        cardRequest.setOwnerId(1L); // User ID
        cardRequest.setInitialBalance(BigDecimal.valueOf(1000.00));

        MvcResult cardResult = mockMvc.perform(post("/api/cards")
                .with(csrf())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andReturn();

        // 2. Create second card
        CreateCardRequest secondCardRequest = new CreateCardRequest();
        secondCardRequest.setOwnerId(1L);
        secondCardRequest.setInitialBalance(BigDecimal.valueOf(500.00));

        mockMvc.perform(post("/api/cards")
                .with(csrf())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondCardRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(500.00));

        // 3. User makes transfer between their own cards
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardId(2L);
        transferRequest.setAmount(BigDecimal.valueOf(200.00));

        mockMvc.perform(post("/api/transfer")
                .with(csrf())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 4. Check card balances
        mockMvc.perform(get("/api/cards/1")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(800.00));

        mockMvc.perform(get("/api/cards/2")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(700.00));

        // 5. Administrator blocks card
        mockMvc.perform(put("/api/cards/1/block")
                .with(csrf())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));

        // 6. Transfer attempt from blocked card should fail
        mockMvc.perform(post("/api/transfer")
                .with(csrf())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void accessControl_UserCannotAccessAdminEndpoints() throws Exception {
        // User cannot create cards
        CreateCardRequest cardRequest = new CreateCardRequest();
        cardRequest.setOwnerId(1L);
        cardRequest.setInitialBalance(BigDecimal.valueOf(1000.00));

        mockMvc.perform(post("/api/cards")
                .with(csrf())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isForbidden());

        // User cannot get list of all cards
        mockMvc.perform(get("/api/cards")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        // User cannot block cards
        mockMvc.perform(put("/api/cards/1/block")
                .with(csrf())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void accessControl_AdminCannotMakeTransfers() throws Exception {
        // Administrator cannot make transfers
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardId(2L);
        transferRequest.setAmount(BigDecimal.valueOf(100.00));

        mockMvc.perform(post("/api/transfer")
                .with(csrf())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void authentication_RequiredForProtectedEndpoints() throws Exception {
        // Access denied without token
        mockMvc.perform(get("/api/cards/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/cards")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/transfer")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    private String extractTokenFromResponse(String response) throws Exception {
        // Parse JSON response and extract token
        return objectMapper.readTree(response).get("accessToken").asText();
    }
}
