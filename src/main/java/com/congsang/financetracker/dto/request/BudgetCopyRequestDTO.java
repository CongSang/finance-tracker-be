package com.congsang.financetracker.dto.request;

import lombok.Data;

@Data
public class BudgetCopyRequestDTO {

    private int fromMonth;
    private int fromYear;
    private int toMonth;
    private int toYear;
}
