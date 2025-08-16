package com.example.bankrest.repository;

import com.example.bankrest.entity.Card;
import com.example.bankrest.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByOwner(User owner);

    List<Card> findByOwnerId(Long ownerId);

    // Add methods with pagination
    Page<Card> findByOwnerId(Long ownerId, Pageable pageable);

    Optional<Card> findByEncryptedCardNumber(String encryptedCardNumber);

    List<Card> findByStatus(Card.CardStatus status);

    // Add methods with pagination for status search
    Page<Card> findByStatus(Card.CardStatus status, Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.owner.id = :ownerId AND c.status = :status")
    List<Card> findByOwnerIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") Card.CardStatus status);

    // Add method with pagination for search by owner and status
    @Query("SELECT c FROM Card c WHERE c.owner.id = :ownerId AND (:status IS NULL OR c.status = :status)")
    Page<Card> findByOwnerIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") Card.CardStatus status, Pageable pageable);

    boolean existsByEncryptedCardNumber(String encryptedCardNumber);
}
