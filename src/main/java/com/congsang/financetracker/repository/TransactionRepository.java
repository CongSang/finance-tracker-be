package com.congsang.financetracker.repository;

import com.congsang.financetracker.common.enums.TransactionType;
import com.congsang.financetracker.dto.response.CashFlowTrendDTO;
import com.congsang.financetracker.dto.response.SpendingCategoryDTO;
import com.congsang.financetracker.entity.CategoryEntity;
import com.congsang.financetracker.entity.TransactionEntity;
import com.congsang.financetracker.entity.UserEntity;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    @Query("SELECT t FROM TransactionEntity t WHERE t.user = :user " +
            "AND (:walletId IS NULL OR t.wallet.id = :walletId) " +
            "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
            "AND (:fromDate IS NULL OR t.transactionDate >= :fromDate) " +
            "AND (:toDate IS NULL OR t.transactionDate <= :toDate) " +
            "AND (:note IS NULL OR t.note LIKE %:note%)")
    Page<TransactionEntity> findByFilters(
            @Param("user") UserEntity user,
            @Param("walletId") Long walletId,
            @Param("categoryId") Long categoryId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("note") String note,
            Pageable pageable
    );

    @NullMarked
    Optional<TransactionEntity> findById(Long id);

    List<TransactionEntity> findByTransferId(Long id);

    @Query("SELECT SUM(t.amount) FROM TransactionEntity t " +
            "WHERE t.category = :category " +
            "AND t.user = :user " +
            "AND t.category.type = 'EXPENSE' " +
            "AND FUNCTION('MONTH', t.transactionDate) = :month " +
            "AND FUNCTION('YEAR', t.transactionDate) = :year")
    BigDecimal sumAmountByCategoryAndMonth(
            @Param("category") CategoryEntity category,
            @Param("month") int month,
            @Param("year") int year,
            @Param("user") UserEntity user
    );

    @Query("SELECT SUM(t.amount) FROM TransactionEntity t " +
            "WHERE t.user = :u " +
            "AND t.category.type = 'EXPENSE' " +
            "AND FUNCTION('MONTH', t.transactionDate) = :m " +
            "AND FUNCTION('YEAR', t.transactionDate) = :y")
    BigDecimal sumSpentByMonthAndYear(@Param("m") int month, @Param("y") int year, @Param("u") UserEntity user);

    @Query("SELECT SUM(t.amount) FROM TransactionEntity t " +
            "WHERE t.user = :user AND t.category.type = :type " +
            "AND FUNCTION('MONTH', t.transactionDate) = :month " +
            "AND FUNCTION('YEAR', t.transactionDate) = :year")
    BigDecimal sumAmountByTypeAndMonth(@Param("user") UserEntity user,
                                       @Param("type") TransactionType type,
                                       @Param("month") int month,
                                       @Param("year") int year);

    @Query("SELECT new com.congsang.financetracker.dto.response.SpendingCategoryDTO(" +
            "t.category.name, t.category.iconUrl, SUM(t.amount), 0.0) " +
            "FROM TransactionEntity t " +
            "WHERE t.user = :user " +
            "AND t.category.type = 'EXPENSE' " +
            "AND EXTRACT(MONTH FROM t.transactionDate) = :month " +
            "AND EXTRACT(YEAR FROM t.transactionDate) = :year " +
            "GROUP BY t.category.id, t.category.name, t.category.iconUrl " +
            "ORDER BY SUM(t.amount) DESC")
    List<SpendingCategoryDTO> getSpendingByCategory(@Param("user") UserEntity user,
                                                    @Param("month") int month,
                                                    @Param("year") int year,
                                                    Pageable pageable);

    @Query("SELECT new com.congsang.financetracker.dto.response.CashFlowTrendDTO(" +
            "FUNCTION('DATE_FORMAT', t.transactionDate, '%d/%m'), " +
            "SUM(CASE WHEN t.category.type = 'INCOME' THEN t.amount ELSE 0 END), " +
            "SUM(CASE WHEN t.category.type = 'EXPENSE' THEN t.amount ELSE 0 END)) " +
            "FROM TransactionEntity t " +
            "WHERE t.user = :user " +
            "AND EXTRACT(MONTH FROM t.transactionDate) = :month " +
            "AND EXTRACT(YEAR FROM t.transactionDate) = :year " +
            "GROUP BY FUNCTION('DATE_FORMAT', t.transactionDate, '%d/%m'), t.transactionDate " +
            "ORDER BY t.transactionDate ASC")
    List<CashFlowTrendDTO> getDailyCashFlow(
            @Param("user") UserEntity user,
            @Param("month") int month,
            @Param("year") int year);

}