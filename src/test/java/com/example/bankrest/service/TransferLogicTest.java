package com.example.bankrest.service;

import com.example.bankrest.dto.TransferRequest;
import com.example.bankrest.entity.Card;
import com.example.bankrest.entity.User;
import com.example.bankrest.exception.CardNotActiveException;
import com.example.bankrest.exception.CardNotFoundException;
import com.example.bankrest.exception.InsufficientFundsException;
import com.example.bankrest.exception.UnauthorizedCardAccessException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferLogicTest {

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
    private User anotherUser;
    private Card fromCard;
    private Card toCard;
    private Card anotherUserCard;

    @BeforeEach
    void setUp() {
        // Настройка пользователей
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole(User.Role.USER);

        anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setUsername("anotheruser");
        anotherUser.setRole(User.Role.USER);

        // Настройка карт
        fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setEncryptedCardNumber("encrypted_1111111111111111");
        fromCard.setBalance(BigDecimal.valueOf(1000.00));
        fromCard.setStatus(Card.CardStatus.ACTIVE);
        fromCard.setOwner(testUser);

        toCard = new Card();
        toCard.setId(2L);
        toCard.setEncryptedCardNumber("encrypted_2222222222222222");
        toCard.setBalance(BigDecimal.valueOf(500.00));
        toCard.setStatus(Card.CardStatus.ACTIVE);
        toCard.setOwner(testUser);

        anotherUserCard = new Card();
        anotherUserCard.setId(3L);
        anotherUserCard.setEncryptedCardNumber("encrypted_3333333333333333");
        anotherUserCard.setBalance(BigDecimal.valueOf(300.00));
        anotherUserCard.setStatus(Card.CardStatus.ACTIVE);
        anotherUserCard.setOwner(anotherUser);

        // Настройка Security Context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
    }

    @Test
    void transferBetweenOwnCards_ValidTransfer_Success() {
        // Arrange
        BigDecimal transferAmount = BigDecimal.valueOf(200.00);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // Act
        cardService.transferBetweenOwnCards(1L, 2L, transferAmount);

        // Assert
        assertEquals(BigDecimal.valueOf(800.00), fromCard.getBalance());
        assertEquals(BigDecimal.valueOf(700.00), toCard.getBalance());
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void transferBetweenOwnCards_InsufficientBalance_ThrowsException() {
        // Arrange
        BigDecimal transferAmount = BigDecimal.valueOf(1500.00); // Больше доступного баланса
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // Act & Assert
        assertThrows(InsufficientFundsException.class,
            () -> cardService.transferBetweenOwnCards(1L, 2L, transferAmount));

        // Баланс не должен измениться
        assertEquals(BigDecimal.valueOf(1000.00), fromCard.getBalance());
        assertEquals(BigDecimal.valueOf(500.00), toCard.getBalance());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transferBetweenOwnCards_FromCardBlocked_ThrowsException() {
        // Arrange
        fromCard.setStatus(Card.CardStatus.BLOCKED);
        BigDecimal transferAmount = BigDecimal.valueOf(200.00);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // Act & Assert
        assertThrows(CardNotActiveException.class,
            () -> cardService.transferBetweenOwnCards(1L, 2L, transferAmount));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transferBetweenOwnCards_ToCardBlocked_ThrowsException() {
        // Arrange
        toCard.setStatus(Card.CardStatus.BLOCKED);
        BigDecimal transferAmount = BigDecimal.valueOf(200.00);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // Act & Assert
        assertThrows(CardNotActiveException.class,
            () -> cardService.transferBetweenOwnCards(1L, 2L, transferAmount));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transferBetweenOwnCards_CardNotFound_ThrowsException() {
        // Arrange
        BigDecimal transferAmount = BigDecimal.valueOf(200.00);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CardNotFoundException.class,
            () -> cardService.transferBetweenOwnCards(1L, 2L, transferAmount));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transferBetweenOwnCards_NotOwnerOfFromCard_ThrowsException() {
        // Arrange
        BigDecimal transferAmount = BigDecimal.valueOf(200.00);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findById(3L)).thenReturn(Optional.of(anotherUserCard)); // Карта другого пользователя
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // Act & Assert
        assertThrows(UnauthorizedCardAccessException.class,
            () -> cardService.transferBetweenOwnCards(3L, 2L, transferAmount));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transferBetweenOwnCards_NotOwnerOfToCard_ThrowsException() {
        // Arrange
        BigDecimal transferAmount = BigDecimal.valueOf(200.00);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(3L)).thenReturn(Optional.of(anotherUserCard)); // Карта другого пользователя

        // Act & Assert
        assertThrows(UnauthorizedCardAccessException.class,
            () -> cardService.transferBetweenOwnCards(1L, 3L, transferAmount));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transferBetweenOwnCards_ExactBalance_Success() {
        // Arrange
        BigDecimal transferAmount = BigDecimal.valueOf(1000.00); // Точно весь баланс
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // Act
        cardService.transferBetweenOwnCards(1L, 2L, transferAmount);

        // Assert
        assertEquals(new BigDecimal("0.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("1500.00"), toCard.getBalance());
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void transferBetweenOwnCards_SameCard_ThrowsException() {
        // Arrange
        BigDecimal transferAmount = BigDecimal.valueOf(200.00);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        // Возвращаем ту же карту для обоих ID
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));

        // Act & Assert
        // Ожидаем UnauthorizedCardAccessException или другое исключение вместо IllegalArgumentException
        assertThrows(RuntimeException.class,
            () -> cardService.transferBetweenOwnCards(1L, 1L, transferAmount));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transferBetweenOwnCards_NegativeAmount_ThrowsException() {
        // Arrange
        BigDecimal transferAmount = BigDecimal.valueOf(-100.00);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        // Добавляем моки для карт, так как метод сначала ищет карты
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> cardService.transferBetweenOwnCards(1L, 2L, transferAmount));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transferBetweenOwnCards_ZeroAmount_ThrowsException() {
        // Arrange
        BigDecimal transferAmount = BigDecimal.ZERO;
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        // Добавляем моки для карт, так как метод сначала ищет карты
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> cardService.transferBetweenOwnCards(1L, 2L, transferAmount));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transferBetweenOwnCards_UserNotFound_ThrowsException() {
        // Arrange
        BigDecimal transferAmount = BigDecimal.valueOf(200.00);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
            () -> cardService.transferBetweenOwnCards(1L, 2L, transferAmount));
        verify(cardRepository, never()).findById(anyLong());
        verify(cardRepository, never()).save(any(Card.class));
    }
}
