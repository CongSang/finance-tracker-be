package com.congsang.financetracker.service;

import com.congsang.financetracker.common.enums.TransactionType;
import com.congsang.financetracker.dto.request.TransferRequestDTO;
import com.congsang.financetracker.entity.*;
import com.congsang.financetracker.exception.BadRequestException;
import com.congsang.financetracker.exception.ResourceNotFoundException;
import com.congsang.financetracker.mapper.TransferMapper;
import com.congsang.financetracker.repository.CategoryRepository;
import com.congsang.financetracker.repository.TransactionRepository;
import com.congsang.financetracker.repository.TransferRepository;
import com.congsang.financetracker.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransferRepository transferRepository;
    private final TransferMapper transferMapper;

    @Transactional
    public void transferMoney(TransferRequestDTO request, UserEntity currentUser) {
        if(Objects.equals(request.getFromWalletId(), request.getToWalletId())) {
            throw new BadRequestException("Không thể chuyển tiền trong cùng 1 ví");
        }

        WalletEntity fromWallet = walletRepository.findById(request.getFromWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví gửi"));
        WalletEntity toWallet = walletRepository.findById(request.getToWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví nhận"));

        if (!fromWallet.getUser().getId().equals(currentUser.getId()) ||
                !toWallet.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền thực hiện giao dịch này");
        }

        if (fromWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Số dư ví gửi không đủ để chuyển khoản");
        }

        CategoryEntity transferCategory = categoryRepository.findByTypeAndUserIsNull(TransactionType.TRANSFER)
                .orElseThrow(() -> new ResourceNotFoundException("Hệ thống thiếu danh mục Chuyển khoản"));

        fromWallet.setBalance(fromWallet.getBalance().subtract(request.getAmount()));
        toWallet.setBalance(toWallet.getBalance().add(request.getAmount()));

        walletRepository.saveAll(List.of(fromWallet, toWallet));

        TransferEntity transfer = transferMapper.toEntity(request);
        transfer.setFromWallet(fromWallet);
        transfer.setToWallet(toWallet);
        transfer.setUser(currentUser);
        transferRepository.save(transfer);

        // Bản ghi CHI (Từ ví gửi)
        TransactionEntity outTx = createTransferRecord(
                request, fromWallet, transferCategory, currentUser,
                "Chuyển khoản đến ví " + toWallet.getName(), transfer);
        // Bản ghi THU (Tại ví nhận)
        TransactionEntity inTx = createTransferRecord(
                request, toWallet, transferCategory, currentUser,
                "Nhận tiền từ ví " + fromWallet.getName(), transfer);

        transactionRepository.saveAll(List.of(outTx, inTx));
    }

    private TransactionEntity createTransferRecord(
            TransferRequestDTO request,
            WalletEntity wallet,
            CategoryEntity category,
            UserEntity user,
            String prefix,
            TransferEntity transfer) {
        TransactionEntity t = new TransactionEntity();
        t.setAmount(request.getAmount());
        t.setNote("[" + prefix + "] " +
                (request.getNote() != null ? request.getNote() : ""));
        t.setTransactionDate(LocalDateTime.now());
        t.setWallet(wallet);
        t.setCategory(category);
        t.setUser(user);
        t.setTransfer(transfer);
        return t;
    }
}
