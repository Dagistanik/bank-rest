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
        // Регистрируем пользователя
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

        // Извлекаем токен пользователя
        String userResponse = userResult.getResponse().getContentAsString();
        userToken = extractTokenFromResponse(userResponse);

        // Регистрируем администратора
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

        // Извлекаем токен администратора
        String adminResponse = adminResult.getResponse().getContentAsString();
        adminToken = extractTokenFromResponse(adminResponse);
    }

    @Test
    void fullBankingWorkflow_Success() throws Exception {
        // 1. Создаем карту как администратор
        CreateCardRequest cardRequest = new CreateCardRequest();
        cardRequest.setOwnerId(1L); // ID пользователя
        cardRequest.setInitialBalance(BigDecimal.valueOf(1000.00));

        MvcResult cardResult = mockMvc.perform(post("/api/cards")
                .with(csrf())
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andReturn();

        // 2. Создаем вторую карту
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

        // 3. Пользователь делает перевод между своими картами
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

        // 4. Проверяем балансы карт
        mockMvc.perform(get("/api/cards/1")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(800.00));

        mockMvc.perform(get("/api/cards/2")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(700.00));

        // 5. Администратор блокирует карту
        mockMvc.perform(put("/api/cards/1/block")
                .with(csrf())
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));

        // 6. Попытка перевода с заблокированной карты должна провалиться
        mockMvc.perform(post("/api/transfer")
                .with(csrf())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void accessControl_UserCannotAccessAdminEndpoints() throws Exception {
        // Пользователь не может создавать карты
        CreateCardRequest cardRequest = new CreateCardRequest();
        cardRequest.setOwnerId(1L);
        cardRequest.setInitialBalance(BigDecimal.valueOf(1000.00));

        mockMvc.perform(post("/api/cards")
                .with(csrf())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isForbidden());

        // Пользователь не может получать список всех карт
        mockMvc.perform(get("/api/cards")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        // Пользователь не может блокировать карты
        mockMvc.perform(put("/api/cards/1/block")
                .with(csrf())
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void accessControl_AdminCannotMakeTransfers() throws Exception {
        // Администратор не может делать переводы
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
        // Без токена доступ запрещен
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
        // Парсим JSON ответ и извлекаем токен
        return objectMapper.readTree(response).get("accessToken").asText();
    }
}
