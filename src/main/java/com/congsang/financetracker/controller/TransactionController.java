package com.congsang.financetracker.controller;

import com.congsang.financetracker.dto.request.PagedRequestDTO;
import com.congsang.financetracker.dto.request.TransactionRequestDTO;
import com.congsang.financetracker.dto.request.WalletRequestDTO;
import com.congsang.financetracker.dto.response.PagedResponseDTO;
import com.congsang.financetracker.dto.response.ScanResponseDTO;
import com.congsang.financetracker.dto.response.TransactionResponseDTO;
import com.congsang.financetracker.dto.response.WalletResponseDTO;
import com.congsang.financetracker.security.UserPrincipal;
import com.congsang.financetracker.service.OcrService;
import com.congsang.financetracker.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final OcrService ocrService;

    @PostMapping
    public ResponseEntity<PagedResponseDTO<TransactionResponseDTO>> getTransactions(
            @RequestBody PagedRequestDTO pageRequest,
            @RequestParam(required = false) Long walletId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) String note,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(transactionService.getTransactions(
                pageRequest, walletId, categoryId, fromDate, toDate, note, currentUser.getUser()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> getTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(transactionService.getTransactionById(id, currentUser.getUser()));
    }

    @PostMapping("/create")
    public ResponseEntity<TransactionResponseDTO> createTransaction(
            @RequestBody TransactionRequestDTO request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(transactionService.createTransaction(request, currentUser.getUser()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDTO> updateTransaction(
            @PathVariable Long id,
            @RequestBody TransactionRequestDTO request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(transactionService.updateTransaction(id, request, currentUser.getUser()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        transactionService.deleteTransaction(id, currentUser.getUser());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/scan")
    public ResponseEntity<?> scan(@RequestBody MultipartFile file) {
        try {
            return ResponseEntity.ok(ocrService.scanInvoice(file));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
