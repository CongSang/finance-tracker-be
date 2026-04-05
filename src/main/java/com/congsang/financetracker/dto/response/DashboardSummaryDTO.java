package com.congsang.financetracker.dto.response;

import com.congsang.financetracker.common.enums.Warning;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryDTO {
    private BigDecimal totalBalance;     // Tổng ví Active
    private BigDecimal totalIncome;      // Thu tháng này
    private BigDecimal totalExpense;     // Chi tháng này
    private double balanceChangePercent; // % thay đổi so với tháng trước
    private Warning healthStatus;        // Thông điệp đánh giá (Quản lý tốt/Cần xem lại)
}
