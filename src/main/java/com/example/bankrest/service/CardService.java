package com.example.bankrest.service;

import com.example.bankrest.dto.CardResponse;
import com.example.bankrest.dto.CreateCardRequest;
import com.example.bankrest.entity.Card;
import com.example.bankrest.entity.User;
import com.example.bankrest.exception.*;
import com.example.bankrest.repository.CardRepository;
import com.example.bankrest.repository.UserRepository;
import com.example.bankrest.util.CardNumberEncryption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardNumberEncryption cardEncryption;

    @PreAuthorize("hasRole('ADMIN')")
    public CardResponse createCard(CreateCardRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new UserNotFoundException(request.getOwnerId()));

        // Generate a random card number
        String cardNumber = cardEncryption.generateCardNumber();
        String encryptedCardNumber = cardEncryption.encrypt(cardNumber);
        // Set default expiry date (3 years) if not specified
        LocalDate expiryDate = request.getExpiryDate() != null ?
                request.getExpiryDate() : LocalDate.now().plusYears(3);

        Card card = new Card();
        card.setEncryptedCardNumber(encryptedCardNumber);
        card.setOwner(owner);
        card.setExpiryDate(expiryDate);
        card.setStatus(Card.CardStatus.ACTIVE);
        card.setBalance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO);

        Card savedCard = cardRepository.save(card);
        return convertToResponse(savedCard);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CardResponse blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + cardId));

        card.setStatus(Card.CardStatus.BLOCKED);
        Card savedCard = cardRepository.save(card);
        return convertToResponse(savedCard);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CardResponse activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + cardId));

        // Check if the card has expired
        if (card.getExpiryDate().isBefore(LocalDate.now())) {
            throw new CardNotActiveException("Cannot activate expired card");
        }

        card.setStatus(Card.CardStatus.ACTIVE);
        Card savedCard = cardRepository.save(card);
        return convertToResponse(savedCard);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + cardId));

        // Check that the card balance is zero
        if (card.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException("Cannot delete card with non-zero balance");
        }

        cardRepository.delete(card);
    }

    @PreAuthorize("hasRole('USER')")
    public List<CardResponse> getCardsByUser() {
        String currentUsername = getCurrentUsername();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        List<Card> userCards = cardRepository.findByOwner(currentUser);
        return userCards.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('USER')")
    public void transferBetweenOwnCards(Long fromCardId, Long toCardId, BigDecimal amount) {
        String currentUsername = getCurrentUsername();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        Card fromCard = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + fromCardId));

        Card toCard = cardRepository.findById(toCardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + toCardId));

        // Check that both cards belong to the current user
        if (!fromCard.getOwner().getId().equals(currentUser.getId()) ||
            !toCard.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedCardAccessException("You can only transfer between your own cards");
        }

        // Check the status of the cards
        if (fromCard.getStatus() != Card.CardStatus.ACTIVE) {
            throw new CardNotActiveException("Source card is not active");
        }

        if (toCard.getStatus() != Card.CardStatus.ACTIVE) {
            throw new CardNotActiveException("Destination card is not active");
        }

        // Check if there are sufficient funds
        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }

        // Check that the amount is positive
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        // Perform the transfer
        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }

    public List<CardResponse> getAllCards() {
        // For admins only - get all cards in the system
        List<Card> allCards = cardRepository.findAll();
        return allCards.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<CardResponse> getAllCardsWithPagination(Pageable pageable, String status, Long ownerId) {
        Page<Card> cards;

        if (status != null && ownerId != null) {
            Card.CardStatus cardStatus = Card.CardStatus.valueOf(status.toUpperCase());
            cards = cardRepository.findByOwnerIdAndStatus(ownerId, cardStatus, pageable);
        } else if (status != null) {
            Card.CardStatus cardStatus = Card.CardStatus.valueOf(status.toUpperCase());
            cards = cardRepository.findByStatus(cardStatus, pageable);
        } else if (ownerId != null) {
            cards = cardRepository.findByOwnerId(ownerId, pageable);
        } else {
            cards = cardRepository.findAll(pageable);
        }

        return cards.map(this::convertToResponse);
    }

    public CardResponse getCardById(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + cardId));

        // Check access rights
        String currentUsername = getCurrentUsername();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("Current user not found"));

        // Admins can view any cards, users can only view their own
        if (!currentUser.getRole().equals(User.Role.ADMIN) &&
            !card.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedCardAccessException("Access denied: You can only view your own cards");
        }

        return convertToResponse(card);
    }

    private CardResponse convertToResponse(Card card) {
        // Decrypt the card number for masking
        String decryptedCardNumber = cardEncryption.decrypt(card.getEncryptedCardNumber());
        String maskedCardNumber = cardEncryption.maskCardNumber(decryptedCardNumber);

        return new CardResponse(
                card.getId(),
                maskedCardNumber,
                card.getOwner().getUsername(),
                card.getExpiryDate(),
                card.getStatus(),
                card.getBalance(),
                card.getCreatedAt()
        );
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
