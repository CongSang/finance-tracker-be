package com.congsang.financetracker.repository;

import com.congsang.financetracker.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // select * from user where email = ?
    Optional<UserEntity> findByEmail(String email);

    // select * from user where activation_token = ?
    Optional<UserEntity> findByActivationToken(String activationToken);

    boolean existsByEmail(String email);
}
