package com.congsang.financetracker.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionRequestDTO {

    private BigDecimal amount;
    private String note;
    private LocalDateTime transactionDate;
    private Long walletId;
    private Long categoryId;
}
