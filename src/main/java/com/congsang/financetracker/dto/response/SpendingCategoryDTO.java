package com.congsang.financetracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpendingCategoryDTO {

    private String categoryName;
    private String categoryIcon;
    private BigDecimal amount;
    private double percentage;
}
