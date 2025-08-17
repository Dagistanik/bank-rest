package com.example.bankrest.service;

import com.example.bankrest.dto.CardResponse;
import com.example.bankrest.dto.CreateCardRequest;
import com.example.bankrest.entity.Card;
import com.example.bankrest.entity.User;
import com.example.bankrest.repository.CardRepository;
import com.example.bankrest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceSimpleTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CardService cardService;

    private User testUser;
    private Card testCard;
    private CreateCardRequest createCardRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(User.Role.USER);

        testCard = new Card();
        testCard.setId(1L);
        testCard.setBalance(BigDecimal.valueOf(1000.00));
        testCard.setStatus(Card.CardStatus.ACTIVE);
        testCard.setOwner(testUser);
        testCard.setCreatedAt(LocalDateTime.now());

        createCardRequest = new CreateCardRequest();
        createCardRequest.setOwnerId(1L);
        createCardRequest.setInitialBalance(BigDecimal.valueOf(500.00));

        // Setup Security Context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
    }

    @Test
    void createCard_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);

        // Act
        CardResponse result = cardService.createCard(createCardRequest);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(1000.00), result.getBalance());
        assertEquals(Card.CardStatus.ACTIVE, result.getStatus());
        assertEquals("testuser", result.getOwnerUsername());

        verify(userRepository).findById(1L);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void createCard_UserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> cardService.createCard(createCardRequest));
        verify(userRepository).findById(1L);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void blockCard_Success() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(testCard)).thenReturn(testCard);

        // Act
        CardResponse result = cardService.blockCard(1L);

        // Assert
        assertNotNull(result);
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
    }

    @Test
    void getCardById_Success() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act
        CardResponse result = cardService.getCardById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(cardRepository).findById(1L);
    }

    @Test
    void getCardById_NotFound() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> cardService.getCardById(1L));
        verify(cardRepository).findById(1L);
    }

    @Test
    void deleteCard_Success() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act
        cardService.deleteCard(1L);

        // Assert
        verify(cardRepository).findById(1L);
        verify(cardRepository).delete(testCard);
    }
}
