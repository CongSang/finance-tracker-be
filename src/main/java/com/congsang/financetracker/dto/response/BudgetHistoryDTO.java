package com.congsang.financetracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetHistoryDTO {
    private String monthYear;      // Ví dụ: "3/2026"
    private BigDecimal totalLimit; // Tổng hạn mức đã đặt
    private BigDecimal totalSpent; // Tổng thực tế đã tiêu
}
