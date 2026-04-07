package com.congsang.financetracker.service;

import com.congsang.financetracker.common.enums.TransactionType;
import com.congsang.financetracker.dto.request.PagedRequestDTO;
import com.congsang.financetracker.dto.request.TransactionRequestDTO;
import com.congsang.financetracker.dto.response.*;
import com.congsang.financetracker.entity.*;
import com.congsang.financetracker.exception.ResourceNotFoundException;
import com.congsang.financetracker.mapper.PagedMapper;
import com.congsang.financetracker.mapper.TransactionMapper;
import com.congsang.financetracker.repository.BudgetRepository;
import com.congsang.financetracker.repository.CategoryRepository;
import com.congsang.financetracker.repository.TransactionRepository;
import com.congsang.financetracker.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final TransactionMapper transactionMapper;
    private final PagedMapper pagedMapper;
    private final BudgetService budgetService;
    private final NotificationService notificationService;

    public PagedResponseDTO<TransactionResponseDTO> getTransactions(
            PagedRequestDTO pagedRequest,
            Long walletId,
            Long categoryId,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String note,
            UserEntity currentUser) {

        Sort sort = pagedRequest.getSortOrder().equalsIgnoreCase("asc")
                ? Sort.by(pagedRequest.getSortField()).ascending()
                : Sort.by(pagedRequest.getSortField()).descending();

        Pageable pageable = PageRequest.of(
                pagedRequest.getPage(),
                pagedRequest.getSize(),
                sort
        );

        Page<TransactionEntity> transactionsPage = transactionRepository.findByFilters(
                currentUser, walletId, categoryId, fromDate, toDate, note, pageable);

        List<TransactionResponseDTO> content = transactionsPage.getContent().stream()
                .map(transactionMapper::toDTO)
                .collect(Collectors.toList());

        return pagedMapper.toDTO(transactionsPage, content);
    }

    public TransactionResponseDTO getTransactionById(Long id, UserEntity currentUser) {
        TransactionEntity transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch với ID: " + id));

        if (!transaction.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền truy cập giao dịch này!");
        }

        return transactionMapper.toDTO(transaction);
    }

    // Hoàn trả lại tiền (Dùng khi xóa hoặc đổi ví)
    private void undoBalance(WalletEntity wallet, CategoryEntity category, BigDecimal amount) {
        if (category.getType() == TransactionType.EXPENSE) {
            wallet.setBalance(wallet.getBalance().add(amount));
        } else {
            wallet.setBalance(wallet.getBalance().subtract(amount));
        }
    }

    // Áp dụng tiền (Dùng khi tạo mới hoặc đổi ví)
    private void applyBalance(WalletEntity wallet, CategoryEntity category, BigDecimal amount) {
        if (category.getType() == TransactionType.EXPENSE) {
            wallet.setBalance(wallet.getBalance().subtract(amount));
        } else {
            wallet.setBalance(wallet.getBalance().add(amount));
        }
    }

    // Xử lý khi chỉ thay đổi tiền/loại danh mục trên cùng 1 ví
    private void updateBalanceSameWallet(
            WalletEntity wallet, CategoryEntity oldCat, BigDecimal oldAmt, CategoryEntity newCat, BigDecimal newAmt) {
        undoBalance(wallet, oldCat, oldAmt);
        applyBalance(wallet, newCat, newAmt);
        walletRepository.save(wallet);
    }

    @Transactional
    public TransactionResponseDTO createTransaction(TransactionRequestDTO request, UserEntity currentUser) {
        WalletEntity wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví"));

        if (!wallet.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền sử dụng ví này");
        }

        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));

        applyBalance(wallet, category, request.getAmount());
        walletRepository.save(wallet);

        // 4. Tạo bản ghi Giao dịch
        TransactionEntity transaction = transactionMapper.toEntity(request);
        transaction.setWallet(wallet);
        transaction.setCategory(category);
        transaction.setUser(currentUser);

        transactionRepository.save(transaction);

        TransactionResponseDTO response = transactionMapper.toDTO(transaction);

        Optional<BudgetEntity> budget = budgetRepository.findByCategoryIdAndMonthAndYearAndUser(
                category.getId(),
                transaction.getTransactionDate().getMonthValue(),
                transaction.getTransactionDate().getYear(),
                currentUser
        );

        BigDecimal actual = transactionRepository.sumAmountByCategoryAndMonth(
                category,
                transaction.getTransactionDate().getMonthValue(),
                transaction.getTransactionDate().getYear(),
                currentUser);
        if (actual == null) actual = BigDecimal.ZERO;

        if (budget.isPresent()) {
            BudgetAnalysisDTO analysis = budgetService.calculateBudgetAnalysis(budget.get(), actual);
            response.setWarning(analysis);
        }

        if (category.getType().equals(TransactionType.EXPENSE)) {
            checkAndNotifyBudget(currentUser, request.getCategoryId());
        }

        return response;
    }

    private void checkAndNotifyBudget(UserEntity user, Long categoryId) {
        LocalDate now = LocalDate.now();
        // Lấy thông tin ngân sách hiện tại của Category này
        budgetRepository.findByCategoryIdAndMonthAndYearAndUser(categoryId, now.getMonthValue(), now.getYear(), user)
                .ifPresent(budget -> {
                    // Tính toán số tiền đã tiêu thực tế
                    BigDecimal spent = transactionRepository.sumAmountByCategoryAndMonth(
                            budget.getCategory(), now.getMonthValue(), now.getYear(), user);

                    // Sử dụng logic phần trăm đã viết ở BudgetService
                    double percent = spent.multiply(new BigDecimal(100))
                            .divide(budget.getAmountLimit(), 2, RoundingMode.HALF_UP).doubleValue();

                    // Gọi service tạo thông báo
                    notificationService.createBudgetNotification(user, budget.getCategory().getName(), percent);
                });
    }

    @Transactional
    public TransactionResponseDTO updateTransaction(Long id, TransactionRequestDTO request, UserEntity currentUser) {
        TransactionEntity transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Giao dịch không tồn tại"));

        if (!transaction.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa giao dịch này");
        }

        WalletEntity oldWallet = transaction.getWallet();
        CategoryEntity oldCategory = transaction.getCategory();
        BigDecimal oldAmount = transaction.getAmount();

        WalletEntity newWallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví để cập nhật giao dịch"));
        CategoryEntity newCategory = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục để cập nhật giao dịch"));
        BigDecimal newAmount = request.getAmount();

        if (oldWallet.getId().equals(newWallet.getId())) {
            // TRƯỜNG HỢP 1: CÙNG MỘT VÍ
            updateBalanceSameWallet(oldWallet, oldCategory, oldAmount, newCategory, newAmount);
        } else {
            // TRƯỜNG HỢP 2: THAY ĐỔI VÍ
            // Hoàn trả ví cũ
            undoBalance(oldWallet, oldCategory, oldAmount);
            // Áp dụng cho ví mới
            applyBalance(newWallet, newCategory, newAmount);

            walletRepository.save(oldWallet);
            walletRepository.save(newWallet);
        }

        transaction.setAmount(newAmount);
        transaction.setCategory(newCategory);
        transaction.setWallet(newWallet);
        transaction.setNote(request.getNote());
        transaction.setTransactionDate(request.getTransactionDate());

        return transactionMapper.toDTO(transactionRepository.save(transaction));
    }

    @Transactional
    public void deleteTransaction(Long id, UserEntity currentUser) {
        TransactionEntity transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch"));

        if (!transaction.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền xóa giao dịch này");
        }

        if (transaction.getTransfer().getId() != null) {
            List<TransactionEntity> relatedTransactions = transactionRepository
                    .findByTransferId(transaction.getTransfer().getId());

            for (TransactionEntity t : relatedTransactions) {
                undoBalanceForTransfer(t);
                walletRepository.save(t.getWallet());
            }

            transactionRepository.deleteAll(relatedTransactions);

        } else {
            undoBalance(transaction.getWallet(), transaction.getCategory(), transaction.getAmount());
            walletRepository.save(transaction.getWallet());
            transactionRepository.delete(transaction);
        }
    }

    private void undoBalanceForTransfer(TransactionEntity transaction) {
        WalletEntity wallet = transaction.getWallet();
        BigDecimal amount = transaction.getAmount();

        if (transaction.getNote().contains("Chuyển khoản đến ví")) {
            wallet.setBalance(wallet.getBalance().add(amount));
        } else if (transaction.getNote().contains("Nhận tiền từ ví")) {
            wallet.setBalance(wallet.getBalance().subtract(amount));
        }
    }
}
