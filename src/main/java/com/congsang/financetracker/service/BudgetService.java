package com.congsang.financetracker.service;

import com.congsang.financetracker.common.enums.TransactionType;
import com.congsang.financetracker.common.enums.Warning;
import com.congsang.financetracker.dto.request.BudgetCopyRequestDTO;
import com.congsang.financetracker.dto.request.BudgetRequestDTO;
import com.congsang.financetracker.dto.response.BudgetAnalysisDTO;
import com.congsang.financetracker.dto.response.BudgetHistoryDTO;
import com.congsang.financetracker.dto.response.BudgetResponseDTO;
import com.congsang.financetracker.entity.BudgetEntity;
import com.congsang.financetracker.entity.CategoryEntity;
import com.congsang.financetracker.entity.UserEntity;
import com.congsang.financetracker.exception.BadRequestException;
import com.congsang.financetracker.exception.ResourceNotFoundException;
import com.congsang.financetracker.mapper.BudgetMapper;
import com.congsang.financetracker.mapper.CategoryMapper;
import com.congsang.financetracker.repository.BudgetRepository;
import com.congsang.financetracker.repository.CategoryRepository;
import com.congsang.financetracker.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetMapper budgetMapper;

    public List<BudgetAnalysisDTO> getBudgetProgress(int month, int year, UserEntity user) {
        List<BudgetEntity> budgets = budgetRepository.findByMonthAndYearAndUser(month, year, user);

        return budgets.stream().map(budget -> {
            BigDecimal actualSpent = transactionRepository.sumAmountByCategoryAndMonth(
                    budget.getCategory(),
                    month,
                    year,
                    user
            );

            if (actualSpent == null) {
                actualSpent = BigDecimal.ZERO;
            }

            return calculateBudgetAnalysis(budget, actualSpent);
        }).collect(Collectors.toList());
    }

    // Cảnh báo chi tiêu
    public BudgetAnalysisDTO calculateBudgetAnalysis(BudgetEntity budget, BigDecimal actualSpent) {
        LocalDate today = LocalDate.now();
        int daysInMonth = today.lengthOfMonth();
        int currentDay = today.getDayOfMonth();
        int remainingDays = daysInMonth - currentDay;

        BigDecimal limit = budget.getAmountLimit();
        BigDecimal remaining = limit.subtract(actualSpent);

        // Tính % đã tiêu
        double percentUsed = 0;
        if (limit.compareTo(BigDecimal.ZERO) > 0) {
            percentUsed = actualSpent.multiply(new BigDecimal(100))
                    .divide(limit, 2, RoundingMode.HALF_UP).doubleValue();
        }

        // Tính Velocity (Tốc độ tiêu tiền)
        double timeRatio = (double) currentDay / daysInMonth;
        double spendingRatio = (limit.compareTo(BigDecimal.ZERO) > 0)
                ? actualSpent.divide(limit, 4, RoundingMode.HALF_UP).doubleValue()
                : 0;

        boolean isFastSpending = (spendingRatio > timeRatio) && (percentUsed < 100);

        // 3. Dự báo ngày hết tiền & Hạn mức chi tiêu hàng ngày còn lại
        Integer projectedDay = null;
        BigDecimal suggestedDaily = BigDecimal.ZERO;

        if (actualSpent.compareTo(BigDecimal.ZERO) > 0) {
            // Chi tiêu trung bình mỗi ngày từ đầu tháng đến giờ
            BigDecimal avgSpendPerDay = actualSpent.divide(BigDecimal.valueOf(currentDay), 2, RoundingMode.HALF_UP);

            // Ngày dự kiến hết tiền = Tổng ngân sách / Chi tiêu trung bình mỗi ngày
            if (avgSpendPerDay.compareTo(BigDecimal.ZERO) > 0) {
                projectedDay = limit.divide(avgSpendPerDay, 0, RoundingMode.FLOOR).intValue();
            } else {
                // Nếu chưa tiêu gì, dự kiến sẽ hết tiền vào ngày cuối cùng của tháng hoặc để mặc định
                projectedDay = daysInMonth;
            }

            // Hạn mức chi tiêu mỗi ngày còn lại để không vỡ kế hoạch
            if (remainingDays > 0 && remaining.compareTo(BigDecimal.ZERO) > 0) {
                suggestedDaily = remaining.divide(BigDecimal.valueOf(remainingDays), 0, RoundingMode.FLOOR);
            }
        }

        Warning status = Warning.GOOD;
        String advice = "Bạn đang kiểm soát tốt chi tiêu.";

        if (percentUsed >= 100) {
            status = Warning.DANGER;
            advice = "Đã vượt hạn mức! Hãy thắt chặt chi tiêu cho " + budget.getCategory().getName() + ".";
        } else if (isFastSpending) {
            status = Warning.WARNING;
            advice = String.format("Cảnh báo: Bạn đang tiêu nhanh hơn dự kiến. Hãy giữ mức %s/ngày.", suggestedDaily.toString());
        } else if (percentUsed >= 80) {
            status = Warning.WARNING;
            advice = "Sắp chạm mốc giới hạn ngân sách tháng này.";
        }

        return new BudgetAnalysisDTO(
                budget.getId(),
                budgetMapper.toDTO(budget),
                limit,
                actualSpent,
                remaining,
                percentUsed,
                status,
                isFastSpending,
                projectedDay,
                suggestedDaily,
                advice
        );
    }

    @Transactional
    public BudgetResponseDTO upsertBudget(BudgetRequestDTO request, UserEntity currentUser) {
        // Tìm xem đã có ngân sách cho Category này trong tháng/năm này chưa
        Optional<BudgetEntity> existingBudget = budgetRepository.findByCategoryIdAndMonthAndYearAndUser(
                request.getCategoryId(), request.getMonth(), request.getYear(), currentUser);

        BudgetEntity budget;
        if (existingBudget.isPresent()) {
            // Cập nhật số tiền mới
            budget = existingBudget.get();
            budget.setAmountLimit(request.getAmountLimit());
        } else {

            // Tạo mới hoàn toàn
            budget = new BudgetEntity();
            CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Danh mục không tồn tại"));


            if (!category.getType().equals(TransactionType.EXPENSE)) {
                throw new BadRequestException("Hạn mức chỉ có thể áp dụng cho danh mục chi tiêu!");
            }

            System.out.println("run");

            budget.setCategory(category);
            budget.setAmountLimit(request.getAmountLimit());
            budget.setMonth(request.getMonth());
            budget.setYear(request.getYear());
            budget.setUser(currentUser);
        }

        return budgetMapper.toDTO(budgetRepository.save(budget));
    }

    @Transactional
    public List<BudgetAnalysisDTO> copyBudgetsFromPreviousMonth(BudgetCopyRequestDTO request, UserEntity currentUser) {
        // 1. Lấy danh sách ngân sách nguồn (tháng trước)
        List<BudgetEntity> sourceBudgets = budgetRepository.findByMonthAndYearAndUser(
                request.getFromMonth(), request.getFromYear(), currentUser);

        if (sourceBudgets.isEmpty()) {
            throw new ResourceNotFoundException("Không có dữ liệu ngân sách từ tháng nguồn để sao chép.");
        }

        // 2. Lấy danh sách ngân sách đích (tháng tới) để tránh trùng lặp
        List<BudgetEntity> targetBudgets = budgetRepository.findByMonthAndYearAndUser(
                request.getToMonth(), request.getToYear(), currentUser);

        // Tạo một Set chứa ID Category đã có ngân sách ở tháng đích
        Set<Long> existingCategoryIds = targetBudgets.stream()
                .map(b -> b.getCategory().getId())
                .collect(Collectors.toSet());

        List<BudgetEntity> newBudgets = new ArrayList<>();

        for (BudgetEntity source : sourceBudgets) {
            // Chỉ sao chép những Category chưa có ngân sách ở tháng đích
            if (!existingCategoryIds.contains(source.getCategory().getId())) {
                BudgetEntity newBudget = new BudgetEntity();
                newBudget.setUser(currentUser);
                newBudget.setCategory(source.getCategory());
                newBudget.setAmountLimit(source.getAmountLimit());
                newBudget.setMonth(request.getToMonth());
                newBudget.setYear(request.getToYear());
                newBudgets.add(newBudget);
            }
        }

        budgetRepository.saveAll(newBudgets);

        return getBudgetProgress(request.getToMonth(), request.getToYear(), currentUser);
    }

    @Transactional
    public void deleteBudget(Long id, UserEntity currentUser) {
        BudgetEntity budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ngân sách với ID: " + id));

        if (!budget.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền xóa ngân sách này!");
        }

        budgetRepository.delete(budget);
    }

    // Báo Cáo 6 Tháng Gần Nhất
    public List<BudgetHistoryDTO> getBudgetHistory(UserEntity user) {
        List<BudgetHistoryDTO> history = new ArrayList<>();
        LocalDate now = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate targetDate = now.minusMonths(i);
            int m = targetDate.getMonthValue();
            int y = targetDate.getYear();

            BigDecimal totalLimit = budgetRepository.sumLimitByMonthAndYear(m, y, user);
            BigDecimal totalSpent = transactionRepository.sumSpentByMonthAndYear(m, y, user);

            // Xử lý null để tránh lỗi ở Frontend
            totalLimit = (totalLimit != null) ? totalLimit : BigDecimal.ZERO;
            totalSpent = (totalSpent != null) ? totalSpent : BigDecimal.ZERO;

            history.add(new BudgetHistoryDTO(m + "/" + y, totalLimit, totalSpent));
        }
        return history;
    }
}
