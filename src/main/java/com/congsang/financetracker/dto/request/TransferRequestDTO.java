package com.congsang.financetracker.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransferRequestDTO {

    private Long fromWalletId;
    private Long toWalletId;
    private BigDecimal amount;
    private String note;
    private LocalDateTime transactionDate;
}
