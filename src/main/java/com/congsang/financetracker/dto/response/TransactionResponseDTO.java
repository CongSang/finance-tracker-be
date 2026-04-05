package com.congsang.financetracker.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponseDTO {

    private Long id;
    private BigDecimal amount;
    private String note;
    private LocalDateTime transactionDate;
    private WalletResponseDTO wallet;
    private CategoryResponseDTO category;
    private BudgetAnalysisDTO warning;
}
