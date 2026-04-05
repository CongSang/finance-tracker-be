package com.congsang.financetracker.service;

import com.congsang.financetracker.common.enums.Warning;
import com.congsang.financetracker.dto.request.PagedRequestDTO;
import com.congsang.financetracker.dto.response.CashFlowTrendDTO;
import com.congsang.financetracker.dto.response.DashboardSummaryDTO;
import com.congsang.financetracker.dto.response.SpendingCategoryDTO;
import com.congsang.financetracker.dto.response.WalletResponseDTO;
import com.congsang.financetracker.entity.UserEntity;
import com.congsang.financetracker.repository.TransactionRepository;
import com.congsang.financetracker.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public DashboardSummaryDTO getOverview(UserEntity user) {
        LocalDate now = LocalDate.now();
        int curMonth = now.getMonthValue();
        int curYear = now.getYear();

        // Tổng số dư hiện tại (Ví Active)
        BigDecimal totalBalance = walletRepository.sumTotalActiveBalanceByUser(user);
        totalBalance = (totalBalance != null) ? totalBalance : BigDecimal.ZERO;

        // Thu/Chi tháng này
        BigDecimal incomeMonth = getSum(user, "INCOME", curMonth, curYear);
        BigDecimal expenseMonth = getSum(user, "EXPENSE", curMonth, curYear);

        // Tính % thay đổi số dư so với tháng trước
        double changePercent = calculateChangePercent(user, incomeMonth, expenseMonth);

        // Đánh giá sức khỏe tài chính
        Warning health = Warning.GOOD;
        if (expenseMonth.compareTo(incomeMonth) > 0) health = Warning.DANGER;
        else if (expenseMonth.multiply(new BigDecimal("0.8")).compareTo(incomeMonth) > 0) health = Warning.WARNING;

        return DashboardSummaryDTO.builder()
                .totalBalance(totalBalance)
                .totalIncome(incomeMonth)
                .totalExpense(expenseMonth)
                .balanceChangePercent(changePercent)
                .healthStatus(health)
                .build();
    }

    private BigDecimal getSum(UserEntity user, String type, int m, int y) {
        BigDecimal sum = transactionRepository.sumAmountByTypeAndMonth(user, type, m, y);
        return (sum != null) ? sum : BigDecimal.ZERO;
    }

    private double calculateChangePercent(UserEntity user, BigDecimal curIncome, BigDecimal curExpense) {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        BigDecimal lastIncome = getSum(user, "INCOME", lastMonth.getMonthValue(), lastMonth.getYear());
        BigDecimal lastExpense = getSum(user, "EXPENSE", lastMonth.getMonthValue(), lastMonth.getYear());

        BigDecimal currentNet = curIncome.subtract(curExpense); // Số tiền dư ra tháng này
        BigDecimal lastNet = lastIncome.subtract(lastExpense);   // Số tiền dư ra tháng trước

        if (lastNet.compareTo(BigDecimal.ZERO) == 0) return 0.0;

        // Công thức: ((Mới - Cũ) / |Cũ|) * 100
        return currentNet.subtract(lastNet)
                .divide(lastNet.abs(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")).doubleValue();
    }

    public List<SpendingCategoryDTO> getSpendingStructure(UserEntity user, int month, int year) {
        // 1. Lấy danh sách chi tiêu đã nhóm theo Category từ DB
        List<SpendingCategoryDTO> list = transactionRepository.getSpendingByCategory(user, month, year);

        // 2. Tính tổng tất cả chi tiêu trong tháng để làm mẫu số
        BigDecimal totalMonthExpense = list.stream()
                .map(SpendingCategoryDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Tính % cho từng hạng mục
        if (totalMonthExpense.compareTo(BigDecimal.ZERO) > 0) {
            list.forEach(item -> {
                double percent = item.getAmount()
                        .multiply(new BigDecimal("100"))
                        .divide(totalMonthExpense, 2, RoundingMode.HALF_UP)
                        .doubleValue();
                item.setPercentage(percent);
            });
        }

        return list;
    }

    public List<CashFlowTrendDTO> getCashFlowTrend(UserEntity user, int month, int year) {
        List<CashFlowTrendDTO> actualData = transactionRepository.getDailyCashFlow(user, month, year);

        Map<String, CashFlowTrendDTO> dataMap = actualData.stream()
                .collect(Collectors.toMap(CashFlowTrendDTO::getDate, d -> d));

        List<CashFlowTrendDTO> fullMonthTrend = new ArrayList<>();
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            String label = String.format("%02d/%02d", day, month);
            fullMonthTrend.add(dataMap.getOrDefault(label,
                    new CashFlowTrendDTO(label, BigDecimal.ZERO, BigDecimal.ZERO)));
        }

        return fullMonthTrend;
    }
}
