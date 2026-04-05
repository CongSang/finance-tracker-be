package com.congsang.financetracker.common.enums;

import lombok.Getter;

@Getter
public enum Frequency {
    DAILY("Hằng ngày"),
    WEEKLY("Hằng tuần"),
    MONTHLY("Hằng tháng");

    private final String displayName;

    Frequency(String displayName) {
        this.displayName = displayName;
    }
}