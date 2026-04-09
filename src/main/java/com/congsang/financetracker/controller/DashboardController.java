package com.congsang.financetracker.controller;

import com.congsang.financetracker.dto.request.FilterRequestDTO;
import com.congsang.financetracker.dto.request.PagedRequestDTO;
import com.congsang.financetracker.dto.response.*;
import com.congsang.financetracker.entity.UserEntity;
import com.congsang.financetracker.security.UserPrincipal;
import com.congsang.financetracker.service.DashboardService;
import com.congsang.financetracker.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;
    private final WalletService walletService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getSummary(@AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(dashboardService.getOverview(user.getUser()));
    }

    @PostMapping("/spending-structure")
    public ResponseEntity<List<SpendingCategoryDTO>> getSpendingStructure(
            @RequestBody FilterRequestDTO request,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(dashboardService.getSpendingStructure(
                user.getUser(), request.getMonth(), request.getYear()));
    }

    @PostMapping("/cash-flow-trend")
    public ResponseEntity<List<CashFlowTrendDTO>> getCashFlowTrend(
            @RequestBody FilterRequestDTO request,
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(dashboardService.getCashFlowTrend(
                user.getUser(), request.getMonth(), request.getYear()));
    }

    @GetMapping("/wallets-mini")
    public ResponseEntity<PagedResponseDTO<WalletResponseDTO>> getWalletsMini(
            @RequestBody PagedRequestDTO page,
            @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(walletService.getAllWallets(page, user));
    }
}