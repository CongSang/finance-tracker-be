package com.congsang.financetracker.mapper;

import com.congsang.financetracker.dto.response.BudgetResponseDTO;
import com.congsang.financetracker.dto.response.WalletResponseDTO;
import com.congsang.financetracker.entity.BudgetEntity;
import com.congsang.financetracker.entity.WalletEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BudgetMapper {

    private final CategoryMapper categoryMapper;

    public BudgetResponseDTO toDTO(BudgetEntity entity) {
        if (entity == null) return null;
        return BudgetResponseDTO.builder()
                .id(entity.getId())
                .amountLimit(entity.getAmountLimit())
                .month(entity.getMonth())
                .year(entity.getYear())
                .category(categoryMapper.toDTO(entity.getCategory()))
                .build();
    }
}
