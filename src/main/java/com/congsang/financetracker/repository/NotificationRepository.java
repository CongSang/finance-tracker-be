package com.congsang.financetracker.repository;

import com.congsang.financetracker.entity.NotificationEntity;
import com.congsang.financetracker.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    List<NotificationEntity> findByUserOrderByCreatedAtDesc(UserEntity user);

    @Modifying
    @Transactional
    @Query("Update NotificationEntity n set n.isRead = true where n.user = :user")
    void markAllAsReadByUser(UserEntity user);
}
