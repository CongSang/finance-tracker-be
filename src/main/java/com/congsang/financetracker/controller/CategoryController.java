package com.congsang.financetracker.controller;

import com.congsang.financetracker.common.enums.Status;
import com.congsang.financetracker.dto.request.CategoryRequestDTO;
import com.congsang.financetracker.dto.response.CategoryResponseDTO;
import com.congsang.financetracker.entity.CategoryEntity;
import com.congsang.financetracker.entity.UserEntity;
import com.congsang.financetracker.exception.BadRequestException;
import com.congsang.financetracker.exception.ResourceNotFoundException;
import com.congsang.financetracker.mapper.CategoryMapper;
import com.congsang.financetracker.repository.CategoryRepository;
import com.congsang.financetracker.security.UserPrincipal;
import com.congsang.financetracker.service.CategoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getCategories(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(categoryService.getCategories(currentUser.getUser()));
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> create(@RequestBody CategoryRequestDTO request, @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(categoryService.createCategory(request, currentUser.getUser()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archiveCategory(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        categoryService.archiveCategory(id, currentUser.getUser());
        return ResponseEntity.noContent().build();
    }
}
