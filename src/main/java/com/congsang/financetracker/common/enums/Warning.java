package com.congsang.financetracker.common.enums;

import lombok.Getter;

@Getter
public enum Warning {

    GOOD("Xanh"),
    WARNING("Vàng"),
    DANGER("Đỏ");

    private final String displayName;

    Warning(String displayName) {
        this.displayName = displayName;
    }
}
