package com.congsang.financetracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class  CashFlowTrendDTO {
    private String date;        // "01/03" hoặc "Tháng 3"
    private BigDecimal income;
    private BigDecimal expense;

    public CashFlowTrendDTO(Object date, Object income, Object expense) {
        this.date = (date != null) ? date.toString() : "";

        switch (income) {
            case Double v -> this.income = BigDecimal.valueOf(v);
            case Long l -> this.income = BigDecimal.valueOf(l);
            case BigDecimal bigDecimal -> this.income = bigDecimal;
            case null, default -> this.income = BigDecimal.ZERO;
        }

        switch (expense) {
            case Double v -> this.expense = BigDecimal.valueOf(v);
            case Long l -> this.expense = BigDecimal.valueOf(l);
            case BigDecimal bigDecimal -> this.expense = bigDecimal;
            case null, default -> this.expense = BigDecimal.ZERO;
        }
    }
}
