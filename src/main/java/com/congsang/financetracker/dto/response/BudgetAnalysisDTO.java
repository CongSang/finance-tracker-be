package com.congsang.financetracker.dto.response;

import com.congsang.financetracker.common.enums.Warning;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetAnalysisDTO {
    // Dữ liệu định danh
    private Long budgetId;
    private CategoryResponseDTO category;

    // Các con số thực tế
    private BigDecimal limitAmount;     // Hạn mức đặt ra (ví dụ: 10.000.000)
    private BigDecimal actualSpent;     // Đã tiêu thực tế (ví dụ: 8.500.000)
    private BigDecimal remainingAmount; // Còn lại (ví dụ: 1.500.000)

    // Tỷ lệ và Trạng thái
    private double percentUsed;         // Phần trăm đã tiêu (ví dụ: 85.0)
    private Warning status;              // SUCCESS, WARNING, DANGER (Dùng để tô màu UI)

    // Cảnh báo nâng cao (Velocity & Forecast)
    private boolean isFastSpending;     // Cảnh báo tốc độ tiêu tiền nhanh
    private Integer projectedExceedDay; // Ngày dự kiến hết tiền (1-31)
    private BigDecimal suggestedDailyLimit; // Hạn mức chi tiêu an toàn mỗi ngày còn lại

    private String adviceMessage;       // Lời khuyên cá nhân hóa (Hiển thị lên Toast/Tooltip)
}
