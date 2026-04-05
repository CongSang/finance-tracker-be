package com.congsang.financetracker.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BudgetResponseDTO {

    private Long id;
    private BigDecimal amountLimit;
    private int month;
    private int year;
    private CategoryResponseDTO category;
}
