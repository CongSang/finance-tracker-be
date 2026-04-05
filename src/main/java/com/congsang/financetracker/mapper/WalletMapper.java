package com.congsang.financetracker.mapper;

import com.congsang.financetracker.dto.request.UserRequestDTO;
import com.congsang.financetracker.dto.request.WalletRequestDTO;
import com.congsang.financetracker.dto.response.UserResponseDTO;
import com.congsang.financetracker.dto.response.WalletResponseDTO;
import com.congsang.financetracker.entity.UserEntity;
import com.congsang.financetracker.entity.WalletEntity;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

    public WalletEntity toEntity(WalletRequestDTO dto, UserEntity user) {
        if (dto == null) return null;
        return WalletEntity.builder()
                .name(dto.getName())
                .balance(dto.getBalance())
                .colorCode(dto.getColorCode())
                .user(user)
                .build();
    }

    public WalletResponseDTO toDTO(WalletEntity entity) {
        if (entity == null) return null;
        return WalletResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .balance(entity.getBalance())
                .currency(entity.getCurrency())
                .colorCode(entity.getColorCode())
                .build();
    }
}
