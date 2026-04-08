package com.congsang.financetracker.controller;

import com.congsang.financetracker.dto.request.BudgetCopyRequestDTO;
import com.congsang.financetracker.dto.request.BudgetRequestDTO;
import com.congsang.financetracker.dto.request.FilterRequestDTO;
import com.congsang.financetracker.dto.response.BudgetAnalysisDTO;
import com.congsang.financetracker.dto.response.BudgetHistoryDTO;
import com.congsang.financetracker.dto.response.BudgetResponseDTO;
import com.congsang.financetracker.security.UserPrincipal;
import com.congsang.financetracker.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetResponseDTO> upsertBudget(
            @RequestBody BudgetRequestDTO request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        return ResponseEntity.ok(budgetService.upsertBudget(request, currentUser.getUser()));
    }

    @PostMapping("/copy")
    public ResponseEntity<List<BudgetAnalysisDTO>> copyBudgets(
            @RequestBody BudgetCopyRequestDTO request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        return ResponseEntity.ok(budgetService.copyBudgetsFromPreviousMonth(request, currentUser.getUser()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        budgetService.deleteBudget(id, currentUser.getUser());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/progress")
    public ResponseEntity<List<BudgetAnalysisDTO>> getBudgetProgress(
            @RequestBody FilterRequestDTO request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(budgetService.getBudgetProgress(request.getMonth(), request.getYear(), currentUser.getUser()));
    }

    @GetMapping("/history")
    public ResponseEntity<List<BudgetHistoryDTO>> getBudgetHistory(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(budgetService.getBudgetHistory(currentUser.getUser()));
    }
}
