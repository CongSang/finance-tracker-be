package com.congsang.financetracker.service;

import com.congsang.financetracker.common.enums.Status;
import com.congsang.financetracker.dto.request.PagedRequestDTO;
import com.congsang.financetracker.dto.request.WalletRequestDTO;
import com.congsang.financetracker.dto.response.PagedResponseDTO;
import com.congsang.financetracker.dto.response.WalletResponseDTO;
import com.congsang.financetracker.entity.UserEntity;
import com.congsang.financetracker.entity.WalletEntity;
import com.congsang.financetracker.exception.BadRequestException;
import com.congsang.financetracker.exception.ResourceNotFoundException;
import com.congsang.financetracker.mapper.PagedMapper;
import com.congsang.financetracker.mapper.WalletMapper;
import com.congsang.financetracker.repository.CategoryRepository;
import com.congsang.financetracker.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;
    private final PagedMapper pagedMapper;

    public PagedResponseDTO<WalletResponseDTO> getAllWallets(
            PagedRequestDTO pagedRequest,
            UserEntity currentUser
    ) {
        Sort sort = pagedRequest.getSortOrder().equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(pagedRequest.getSortField()).ascending()
                : Sort.by(pagedRequest.getSortField()).descending();

        Pageable pageable = (Pageable) PageRequest.of(
                pagedRequest.getPage(),
                pagedRequest.getSize(),
                sort
        );

        Page<WalletEntity> walletsPage = walletRepository.findActiveWalletsByUser(currentUser, pageable);

        List<WalletResponseDTO> content = walletsPage.getContent().stream()
                .map(walletMapper::toDTO)
                .collect(Collectors.toList());

        return pagedMapper.toDTO(walletsPage, content);
    }

    public WalletResponseDTO getWalletById(Long id, UserEntity currentUser) {
        WalletEntity wallet = walletRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví với ID: " + id));

        if (!wallet.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền truy cập ví này!");
        }

        return walletMapper.toDTO(wallet);
    }

    @Transactional
    public WalletResponseDTO createWallet(WalletRequestDTO request, UserEntity currentUser) {
        Optional<WalletEntity> existingWallet = walletRepository.findByNameAndUser(request.getName(), currentUser);

        if (existingWallet.isPresent()) {
            WalletEntity wallet = existingWallet.get();

            if (wallet.getStatus() == Status.ACTIVE) {
                throw new BadRequestException("Bạn đã có một ví tên là: " + request.getName());
            }

            wallet.setStatus(Status.ACTIVE);
            wallet.setBalance(request.getBalance());
            wallet.setCurrency(request.getCurrency());
            wallet.setColorCode(request.getColorCode());

            return walletMapper.toDTO(walletRepository.save(wallet));
        }

        WalletEntity wallet = walletMapper.toEntity(request, currentUser);
        return walletMapper.toDTO(walletRepository.save(wallet));
    }

    @Transactional
    public WalletResponseDTO updateWallet(Long id, WalletRequestDTO request, UserEntity currentUser) {
        WalletEntity oldWallet = walletRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví"));

        if (!oldWallet.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền sửa ví này");
        }

        if (!oldWallet.getName().equals(request.getName())) {
            boolean exists = walletRepository.existsByNameAndUserAndIdNot(
                    request.getName(), currentUser, id);
            if (exists) {
                throw new BadRequestException(
                        "Tên ví '" + request.getName() + "' đã được sử dụng!");
            }
        }

        WalletEntity wallet = walletMapper.toEntity(request, currentUser);
        wallet.setId(id);
        return walletMapper.toDTO(walletRepository.save(wallet));
    }

    @Transactional
    public void archiveWallet(Long id, UserEntity currentUser) {
        WalletEntity wallet = walletRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ví"));

        if (!wallet.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền tác động vào ví này");
        }

        wallet.setStatus(Status.INACTIVE);
        walletRepository.save(wallet);
    }

    public List<WalletResponseDTO> getWalletDropdown(UserEntity currentUser) {
        return walletRepository.findAllVisibleToUser(currentUser)
                .stream().map(walletMapper::toDTO).toList();
    }
}
