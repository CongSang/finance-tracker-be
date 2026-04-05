package com.congsang.financetracker.repository;

import com.congsang.financetracker.entity.BudgetEntity;
import com.congsang.financetracker.entity.CategoryEntity;
import com.congsang.financetracker.entity.UserEntity;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<BudgetEntity, Long> {

    List<BudgetEntity> findByMonthAndYearAndUser(int month, int year, UserEntity user);

    Optional<BudgetEntity> findByCategoryIdAndMonthAndYearAndUser(
            Long categoryId, int month, int year, UserEntity user);

    @Query("SELECT SUM(b.amountLimit) FROM BudgetEntity b WHERE b.month = :m AND b.year = :y AND b.user = :u")
    BigDecimal sumLimitByMonthAndYear(@Param("m") int month, @Param("y") int year, @Param("u") UserEntity user);

    @NullMarked
    Optional<BudgetEntity> findById(Long id);
}
