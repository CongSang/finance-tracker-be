package com.congsang.financetracker.repository;

import com.congsang.financetracker.common.enums.TransactionType;
import com.congsang.financetracker.entity.CategoryEntity;
import com.congsang.financetracker.entity.UserEntity;
import com.congsang.financetracker.entity.WalletEntity;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    @Query("SELECT c FROM CategoryEntity c WHERE (c.user IS NULL OR c.user = :user) AND c.status = 'ACTIVE'")
    List<CategoryEntity> findAllVisibleToUser(UserEntity user);

    Optional<CategoryEntity> findByNameAndTypeAndUser(String name, TransactionType type, UserEntity user);

    @NullMarked
    Optional<CategoryEntity> findById(Long id);

    Optional<CategoryEntity> findByNameAndUserIsNull(String name);
}
