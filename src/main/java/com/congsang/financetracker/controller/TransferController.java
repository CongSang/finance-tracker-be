package com.congsang.financetracker.controller;

import com.congsang.financetracker.dto.request.TransferRequestDTO;
import com.congsang.financetracker.dto.request.WalletRequestDTO;
import com.congsang.financetracker.dto.response.WalletResponseDTO;
import com.congsang.financetracker.entity.TransferEntity;
import com.congsang.financetracker.security.UserPrincipal;
import com.congsang.financetracker.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<Void> transfer(
            @RequestBody TransferRequestDTO request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        transferService.transferMoney(request, currentUser.getUser());
        return ResponseEntity.ok().build();
    }
}
