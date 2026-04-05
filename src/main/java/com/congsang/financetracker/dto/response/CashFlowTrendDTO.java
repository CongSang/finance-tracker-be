package com.congsang.financetracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class CashFlowTrendDTO {
    private String date;        // "01/03" hoặc "Tháng 3"
    private BigDecimal income;
    private BigDecimal expense;

    public CashFlowTrendDTO(Object date, Object income, Object expense) {
        this.date = (date != null) ? date.toString() : "";
        this.income = (income != null) ? new BigDecimal(income.toString()) : BigDecimal.ZERO;
        this.expense = (expense != null) ? new BigDecimal(expense.toString()) : BigDecimal.ZERO;
    }
}
