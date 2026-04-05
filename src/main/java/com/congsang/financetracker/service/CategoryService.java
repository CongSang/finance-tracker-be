package com.congsang.financetracker.service;

import com.congsang.financetracker.common.enums.Status;
import com.congsang.financetracker.dto.request.CategoryRequestDTO;
import com.congsang.financetracker.dto.response.CategoryResponseDTO;
import com.congsang.financetracker.entity.CategoryEntity;
import com.congsang.financetracker.entity.UserEntity;
import com.congsang.financetracker.exception.BadRequestException;
import com.congsang.financetracker.exception.ResourceNotFoundException;
import com.congsang.financetracker.mapper.CategoryMapper;
import com.congsang.financetracker.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryResponseDTO> getCategories(UserEntity currentUser) {
        return categoryRepository.findAllVisibleToUser(currentUser)
                .stream().map(categoryMapper::toDTO).toList();
    }

    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO request, UserEntity currentUser) {
        Optional<CategoryEntity> existingCategory = categoryRepository
                .findByNameAndTypeAndUser(request.getName(), request.getType(), currentUser);

        if (existingCategory.isPresent()) {
            CategoryEntity category = existingCategory.get();

            if (category.getStatus() == Status.ACTIVE) {
                throw new BadRequestException("Danh mục này đã tồn tại!");
            }
        }

        CategoryEntity category = categoryMapper.toEntity(request, currentUser);
        return categoryMapper.toDTO(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO request, UserEntity currentUser) {
        CategoryEntity oldCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));

        if (oldCategory.getUser() == null) {
            throw new AccessDeniedException("Bạn không có quyền sửa danh mục hệ thống");
        }

        if (!oldCategory.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền sửa danh mục này");
        }

        if (!oldCategory.getName().equals(request.getName())) {
            boolean exists = categoryRepository.existsByNameAndUserAndIdNot(
                    request.getName(), currentUser, id);
            if (exists) {
                throw new BadRequestException(
                        "Tên danh mục '" + request.getName() + "' đã được sử dụng!");
            }
        }

        CategoryEntity category = categoryMapper.toEntity(request, currentUser);
        category.setId(id);
        return categoryMapper.toDTO(categoryRepository.save(category));
    }

    @Transactional
    public void archiveCategory(Long id, UserEntity currentUser) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));

        // Không cho phép xóa danh mục của Hệ thống (user == null)
        if (category.getUser() == null) {
            throw new AccessDeniedException("Bạn không thể xóa danh mục mặc định của hệ thống");
        }

        if (!category.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Bạn không có quyền xóa danh mục này");
        }

        category.setStatus(Status.INACTIVE);
        categoryRepository.save(category);
    }
}
