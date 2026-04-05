package com.congsang.financetracker.mapper;

import com.congsang.financetracker.common.enums.Notification;
import com.congsang.financetracker.dto.request.UserRequestDTO;
import com.congsang.financetracker.dto.response.NotificationResponseDTO;
import com.congsang.financetracker.dto.response.UserResponseDTO;
import com.congsang.financetracker.entity.NotificationEntity;
import com.congsang.financetracker.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationEntity toEntity(
            String title,
            String message,
            Notification type) {
        return NotificationEntity.builder()
                .title(title)
                .message(message)
                .type(type)
                .build();
    }

    public NotificationResponseDTO toDTO(NotificationEntity entity) {
        if (entity == null) return null;
        return NotificationResponseDTO.builder()
                .title(entity.getTitle())
                .message(entity.getMessage())
                .type(entity.getType())
                .isRead(entity.isRead())
                .id(entity.getId())
                .build();
    }
}
