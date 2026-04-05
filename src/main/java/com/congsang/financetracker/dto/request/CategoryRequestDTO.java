package com.congsang.financetracker.dto.request;

import com.congsang.financetracker.common.enums.TransactionType;
import lombok.Data;

@Data
public class CategoryRequestDTO {

    private String name;
    private TransactionType type;
    private String iconUrl;
}
