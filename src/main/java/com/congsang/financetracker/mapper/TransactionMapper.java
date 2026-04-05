package com.congsang.financetracker.mapper;

import com.congsang.financetracker.dto.request.TransactionRequestDTO;
import com.congsang.financetracker.dto.response.TransactionResponseDTO;
import com.congsang.financetracker.entity.TransactionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TransactionMapper {

    private final CategoryMapper categoryMapper;
    private final WalletMapper walletMapper;

    public TransactionEntity toEntity(TransactionRequestDTO dto) {
        if (dto == null) return null;
        return TransactionEntity.builder()
                .note(dto.getNote())
                .amount(dto.getAmount())
                .transactionDate(dto.getTransactionDate() != null
                        ? dto.getTransactionDate() : LocalDateTime.now())
                .build();
    }

    public TransactionResponseDTO toDTO(TransactionEntity entity) {
        if (entity == null) return null;
        return TransactionResponseDTO.builder()
                .note(entity.getNote())
                .amount(entity.getAmount())
                .transactionDate(entity.getTransactionDate())
                .category(categoryMapper.toDTO(entity.getCategory()))
                .wallet(walletMapper.toDTO((entity.getWallet())))
                .build();
    }
}
