package com.congsang.financetracker.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletRequestDTO {

    @NotNull(message = "Tên ví không được để trống")
    private String name;

    @NotNull(message = "Số tiền không được để trống")
    @Min(value = 0, message = "Số tiền phải lớn hơn hoặc bằng 0")
    private BigDecimal balance;

    private String currency;

    @NotNull(message = "Màu ví không được để trống")
    private String colorCode;
}
