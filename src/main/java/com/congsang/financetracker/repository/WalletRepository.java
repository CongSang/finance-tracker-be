package com.congsang.financetracker.repository;

import com.congsang.financetracker.entity.UserEntity;
import com.congsang.financetracker.entity.WalletEntity;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


public interface WalletRepository extends JpaRepository<WalletEntity, Long> {

    @Query("SELECT w FROM WalletEntity w WHERE w.user = :user AND w.status = 'ACTIVE'")
    Page<WalletEntity> findActiveWalletsByUser(UserEntity user, Pageable pageable);

    Optional<WalletEntity> findByNameAndUser(String name, UserEntity user);

    @NullMarked
    Optional<WalletEntity> findById(Long id);

    boolean existsByNameAndUserAndIdNot(String name, UserEntity user, Long id);

    // Tính tổng tiền đang "Sẵn sàng chi tiêu" (Chỉ ví Active)
    @Query("SELECT SUM(w.balance) FROM WalletEntity w WHERE w.user = :user AND w.status = 'ACTIVE'")
    BigDecimal sumTotalActiveBalanceByUser(UserEntity user);

    @Query("SELECT w FROM WalletEntity w WHERE w.user = :user AND w.status = 'ACTIVE'")
    List<WalletEntity> findAllVisibleToUser(UserEntity user);

    // Tính tổng tài sản thực tế (Tất cả ví)
    @Query("SELECT SUM(w.balance) FROM WalletEntity w WHERE w.user = :user")
    BigDecimal sumTotalBalanceByUser(UserEntity user);
}
