package com.example.bankrest.service;

import com.example.bankrest.dto.CardResponse;
import com.example.bankrest.dto.CreateCardRequest;
import com.example.bankrest.entity.Card;
import com.example.bankrest.entity.User;
import com.example.bankrest.exception.CardNotActiveException;
import com.example.bankrest.exception.CardNotFoundException;
import com.example.bankrest.exception.InsufficientFundsException;
import com.example.bankrest.exception.UnauthorizedCardAccessException;
import com.example.bankrest.repository.CardRepository;
import com.example.bankrest.repository.UserRepository;
import com.example.bankrest.util.CardNumberEncryption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardNumberEncryption cardEncryption;

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
        // Настройка тестовых данных
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(User.Role.USER);

        testCard = new Card();
        testCard.setId(1L);
        testCard.setEncryptedCardNumber("encrypted_1234567890123456");
        testCard.setBalance(BigDecimal.valueOf(1000.00));
        testCard.setStatus(Card.CardStatus.ACTIVE);
        testCard.setOwner(testUser);
        testCard.setCreatedAt(LocalDateTime.now());
        testCard.setExpiryDate(LocalDate.now().plusYears(3));

        createCardRequest = new CreateCardRequest();
        createCardRequest.setOwnerId(1L);
        createCardRequest.setInitialBalance(BigDecimal.valueOf(500.00));

        // Security Context будет настраиваться в конкретных тестах где нужен
    }

    private void setupSecurityContext() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
    }

    @Test
    void createCard_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardEncryption.generateCardNumber()).thenReturn("1234567890123456");
        when(cardEncryption.encrypt("1234567890123456")).thenReturn("encrypted_1234567890123456");
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardEncryption.decrypt("encrypted_1234567890123456")).thenReturn("1234567890123456");
        when(cardEncryption.maskCardNumber("1234567890123456")).thenReturn("****-****-****-3456");

        // Act
        CardResponse result = cardService.createCard(createCardRequest);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(1000.00), result.getBalance());
        assertEquals(Card.CardStatus.ACTIVE, result.getStatus());
        assertEquals("testuser", result.getOwnerUsername());

        verify(userRepository).findById(1L);
        verify(cardEncryption).generateCardNumber();
        verify(cardEncryption).encrypt("1234567890123456");
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
        when(cardEncryption.decrypt("encrypted_1234567890123456")).thenReturn("1234567890123456");
        when(cardEncryption.maskCardNumber("1234567890123456")).thenReturn("****-****-****-3456");

        testCard.setStatus(Card.CardStatus.BLOCKED);
        when(cardRepository.save(testCard)).thenReturn(testCard);

        // Act
        CardResponse result = cardService.blockCard(1L);

        // Assert
        assertNotNull(result);
        assertEquals(Card.CardStatus.BLOCKED, result.getStatus());
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
    }

    @Test
    void blockCard_CardNotFound() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CardNotFoundException.class, () -> cardService.blockCard(1L));
        verify(cardRepository).findById(1L);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void activateCard_Success() {
        // Arrange
        testCard.setStatus(Card.CardStatus.BLOCKED);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardEncryption.decrypt("encrypted_1234567890123456")).thenReturn("1234567890123456");
        when(cardEncryption.maskCardNumber("1234567890123456")).thenReturn("****-****-****-3456");

        testCard.setStatus(Card.CardStatus.ACTIVE);
        when(cardRepository.save(testCard)).thenReturn(testCard);

        // Act
        CardResponse result = cardService.activateCard(1L);

        // Assert
        assertNotNull(result);
        assertEquals(Card.CardStatus.ACTIVE, result.getStatus());
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(testCard);
    }

    @Test
    void transferBetweenOwnCards_Success() {
        // Arrange
        setupSecurityContext(); // Настраиваем Security Context только для этого теста

        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setBalance(BigDecimal.valueOf(1000.00));
        fromCard.setStatus(Card.CardStatus.ACTIVE);
        fromCard.setOwner(testUser);

        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setBalance(BigDecimal.valueOf(500.00));
        toCard.setStatus(Card.CardStatus.ACTIVE);
        toCard.setOwner(testUser);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        BigDecimal transferAmount = BigDecimal.valueOf(200.00);

        // Act
        cardService.transferBetweenOwnCards(1L, 2L, transferAmount);

        // Assert
        assertEquals(BigDecimal.valueOf(800.00), fromCard.getBalance());
        assertEquals(BigDecimal.valueOf(700.00), toCard.getBalance());
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void transferBetweenOwnCards_InsufficientBalance() {
        // Arrange
        setupSecurityContext(); // Настраиваем Security Context только для этого теста

        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setBalance(BigDecimal.valueOf(100.00));
        fromCard.setStatus(Card.CardStatus.ACTIVE);
        fromCard.setOwner(testUser);

        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setBalance(BigDecimal.valueOf(500.00));
        toCard.setStatus(Card.CardStatus.ACTIVE);
        toCard.setOwner(testUser);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        BigDecimal transferAmount = BigDecimal.valueOf(200.00);

        // Act & Assert
        assertThrows(InsufficientFundsException.class,
            () -> cardService.transferBetweenOwnCards(1L, 2L, transferAmount));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transferBetweenOwnCards_FromCardNotActive() {
        // Arrange
        setupSecurityContext(); // Настраиваем Security Context только для этого теста

        Card fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setBalance(BigDecimal.valueOf(1000.00));
        fromCard.setStatus(Card.CardStatus.BLOCKED);
        fromCard.setOwner(testUser);

        Card toCard = new Card();
        toCard.setId(2L);
        toCard.setBalance(BigDecimal.valueOf(500.00));
        toCard.setStatus(Card.CardStatus.ACTIVE);
        toCard.setOwner(testUser);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        BigDecimal transferAmount = BigDecimal.valueOf(200.00);

        // Act & Assert
        assertThrows(CardNotActiveException.class,
            () -> cardService.transferBetweenOwnCards(1L, 2L, transferAmount));
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void getAllCardsWithPagination_Success() {
        // Arrange
        List<Card> cards = Arrays.asList(testCard);
        Page<Card> cardPage = new PageImpl<>(cards);
        Pageable pageable = PageRequest.of(0, 10);

        when(cardRepository.findAll(pageable)).thenReturn(cardPage);
        // Добавляем моки для шифрования, которые нужны в convertToResponse()
        when(cardEncryption.decrypt("encrypted_1234567890123456")).thenReturn("1234567890123456");
        when(cardEncryption.maskCardNumber("1234567890123456")).thenReturn("****-****-****-3456");

        // Act
        Page<CardResponse> result = cardService.getAllCardsWithPagination(pageable, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("testuser", result.getContent().get(0).getOwnerUsername());
        verify(cardRepository).findAll(pageable);
    }

    @Test
    void getCardById_Success() {
        // Arrange
        setupSecurityContext(); // Настраиваем Security Context только для этого теста

        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(cardEncryption.decrypt("encrypted_1234567890123456")).thenReturn("1234567890123456");
        when(cardEncryption.maskCardNumber("1234567890123456")).thenReturn("****-****-****-3456");

        // Act
        CardResponse result = cardService.getCardById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("****-****-****-3456", result.getMaskedCardNumber());
        assertEquals("testuser", result.getOwnerUsername());
        verify(cardRepository).findById(1L);
    }

    @Test
    void getCardById_NotFound() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CardNotFoundException.class, () -> cardService.getCardById(1L));
        verify(cardRepository).findById(1L);
    }

    @Test
    void deleteCard_Success() {
        // Arrange
        testCard.setBalance(BigDecimal.ZERO); // Устанавливаем нулевой баланс для успешного удаления
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act
        cardService.deleteCard(1L);

        // Assert
        verify(cardRepository).findById(1L);
        verify(cardRepository).delete(testCard);
    }

    @Test
    void deleteCard_NotFound() {
        // Arrange
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CardNotFoundException.class, () -> cardService.deleteCard(1L));
        verify(cardRepository).findById(1L);
        verify(cardRepository, never()).delete(any(Card.class));
    }
}
