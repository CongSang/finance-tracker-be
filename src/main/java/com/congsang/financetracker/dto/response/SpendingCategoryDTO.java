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

    public SpendingCategoryDTO(String name, String iconUrl, Object totalAmount, Double percentage) {
        this.categoryName = name;
        this.categoryIcon = iconUrl;
        this.percentage = percentage;

        switch (totalAmount) {
            case Double v -> this.amount = BigDecimal.valueOf(v);
            case Long l -> this.amount = BigDecimal.valueOf(l);
            case BigDecimal bigDecimal -> this.amount = bigDecimal;
            case null, default -> this.amount = BigDecimal.ZERO;
        }
    }
}
