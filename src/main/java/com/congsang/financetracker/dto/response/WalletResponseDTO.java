package com.congsang.financetracker.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WalletResponseDTO {
    private Long id;
    private String name;
    private BigDecimal balance;
    private String currency;
    private String colorCode;
}
