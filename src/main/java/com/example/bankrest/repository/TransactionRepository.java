package com.example.bankrest.repository;

import com.example.bankrest.entity.Transaction;
import com.example.bankrest.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByFromCard(Card fromCard);

    List<Transaction> findByToCard(Card toCard);

    @Query("SELECT t FROM Transaction t WHERE t.fromCard.id = :cardId OR t.toCard.id = :cardId ORDER BY t.transactionDate DESC")
    List<Transaction> findByCardId(@Param("cardId") Long cardId);

    @Query("SELECT t FROM Transaction t WHERE (t.fromCard.owner.id = :userId OR t.toCard.owner.id = :userId) ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserId(@Param("userId") Long userId);

    List<Transaction> findByStatus(Transaction.TransactionStatus status);

    @Query("SELECT t FROM Transaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByTransactionDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Transaction t WHERE (t.fromCard.id = :cardId OR t.toCard.id = :cardId) AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByCardIdAndDateRange(@Param("cardId") Long cardId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
