package com.congsang.financetracker.mapper;

import com.congsang.financetracker.dto.request.TransferRequestDTO;
import com.congsang.financetracker.dto.request.UserRequestDTO;
import com.congsang.financetracker.dto.response.UserResponseDTO;
import com.congsang.financetracker.entity.TransferEntity;
import com.congsang.financetracker.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class TransferMapper {

    public TransferEntity toEntity(TransferRequestDTO dto) {
        if (dto == null) return null;
        return TransferEntity.builder()
                .note(dto.getNote())
                .amount(dto.getAmount())
                .build();
    }
}
