package com.congsang.financetracker.mapper;

import com.congsang.financetracker.dto.request.CategoryRequestDTO;
import com.congsang.financetracker.dto.request.UserRequestDTO;
import com.congsang.financetracker.dto.response.CategoryResponseDTO;
import com.congsang.financetracker.dto.response.UserResponseDTO;
import com.congsang.financetracker.entity.CategoryEntity;
import com.congsang.financetracker.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryEntity toEntity(CategoryRequestDTO dto) {
        if (dto == null) return null;
        return CategoryEntity.builder()
                .name(dto.getName())
                .type(dto.getType())
                .iconUrl(dto.getIconUrl())
                .build();
    }

    public CategoryResponseDTO toDTO(CategoryEntity entity) {
        if (entity == null) return null;
        return CategoryResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .type(entity.getType())
                .iconUrl(entity.getIconUrl())
                .build();
    }
}
