package com.congsang.financetracker.mapper;


import com.congsang.financetracker.dto.request.UserRequestDTO;
import com.congsang.financetracker.dto.response.UserResponseDTO;
import com.congsang.financetracker.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserEntity toEntity(UserRequestDTO dto) {
        if (dto == null) return null;
        return UserEntity.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .avatarUrl(dto.getAvatarUrl())
                .build();
    }

    public UserResponseDTO toDTO(UserEntity entity) {
        if (entity == null) return null;
        return UserResponseDTO.builder()
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .avatarUrl(entity.getAvatarUrl())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
