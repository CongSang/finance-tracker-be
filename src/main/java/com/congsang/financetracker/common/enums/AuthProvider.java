package com.congsang.financetracker.common.enums;

import lombok.Getter;

@Getter
public enum AuthProvider {
    LOCAL("Đăng nhập bằng hệ thống"),
    GOOGLE("Đăng nhập bằng Google");

    private final String displayName;

    AuthProvider(String displayName) {
        this.displayName = displayName;
    }
}

