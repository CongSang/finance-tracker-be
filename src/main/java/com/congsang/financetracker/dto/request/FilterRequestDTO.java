package com.congsang.financetracker.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class FilterRequestDTO {

    @Min(1) @Max(12)
    private int month;

    @Min(2024)
    private int year;
}
