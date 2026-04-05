package com.congsang.financetracker.common.enums;

import lombok.Getter;

@Getter
public enum Notification {

    BUDGET_EXCEEDED("Vượt quá chi tiêu"),
    BUDGET_WARNING("Cảnh báo chi tiêu"),
    SYSTEM("Hệ thống");

    private final String displayName;

    Notification(String displayName) {
        this.displayName = displayName;
    }
}
