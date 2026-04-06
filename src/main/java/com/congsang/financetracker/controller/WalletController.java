package com.congsang.financetracker.controller;

import com.congsang.financetracker.dto.request.PagedRequestDTO;
import com.congsang.financetracker.dto.request.WalletRequestDTO;
import com.congsang.financetracker.dto.response.PagedResponseDTO;
import com.congsang.financetracker.dto.response.WalletResponseDTO;
import com.congsang.financetracker.security.UserPrincipal;
import com.congsang.financetracker.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<PagedResponseDTO<WalletResponseDTO>> getWallets(
            @RequestBody PagedRequestDTO page,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(walletService.getAllWallets(page, currentUser.getUser()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WalletResponseDTO> getWallet(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(walletService.getWalletById(id, currentUser.getUser()));
    }

    @PostMapping("/create")
    public ResponseEntity<WalletResponseDTO> createWallet(
            @RequestBody WalletRequestDTO request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(walletService.createWallet(request, currentUser.getUser()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WalletResponseDTO> updateWallet(
            @PathVariable Long id,
            @RequestBody WalletRequestDTO request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(walletService.updateWallet(id, request, currentUser.getUser()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archiveWallet(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        walletService.archiveWallet(id, currentUser.getUser());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dropdown")
    public ResponseEntity<List<WalletResponseDTO>> getWalletDropdown(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(walletService.getWalletDropdown(currentUser.getUser()));
    }
}
