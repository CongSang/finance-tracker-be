package com.congsang.financetracker.dto.response;

import com.congsang.financetracker.common.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponseDTO {

    private Long id;
    private String name;
    private TransactionType type;
    private String iconUrl;
}
