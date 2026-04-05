package com.congsang.financetracker.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetRequestDTO {

    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;

    @NotNull(message = "Số tiền hạn mức không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Hạn mức phải lớn hơn 0")
    private BigDecimal amountLimit;

    @Min(1) @Max(12)
    private int month;

    @Min(2024)
    private int year;
}
