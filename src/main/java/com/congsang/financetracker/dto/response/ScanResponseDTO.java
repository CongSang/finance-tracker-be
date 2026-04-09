package com.congsang.financetracker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScanResponseDTO {

    private BigDecimal amount;
    private String transactionDate;
    private String note;
    private String category;
}
