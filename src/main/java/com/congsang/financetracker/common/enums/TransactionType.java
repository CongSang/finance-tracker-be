package com.congsang.financetracker.common.enums;

import lombok.Getter;

@Getter
public enum TransactionType {
    INCOME("Thu nhập"),
    EXPENSE("Chi tiêu"),
    TRANSFER("Chuyển khoản (Nội bộ)");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }
}
