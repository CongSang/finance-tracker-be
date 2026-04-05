package com.congsang.financetracker.repository;

import com.congsang.financetracker.entity.TransferEntity;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransferRepository extends JpaRepository<TransferEntity, Long> {

    @NullMarked
    Optional<TransferEntity> findById(Long id);
}
