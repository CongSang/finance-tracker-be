package com.congsang.financetracker.mapper;

import com.congsang.financetracker.dto.request.PagedRequestDTO;
import com.congsang.financetracker.dto.response.PagedResponseDTO;
import com.congsang.financetracker.dto.response.WalletResponseDTO;
import com.congsang.financetracker.entity.WalletEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PagedMapper {

    public <T> PagedResponseDTO<T> toDTO(Page<?> page, List<T> data) {
        return PagedResponseDTO.<T>builder()
                .pageNo(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .content(data)
                .isLast(page.isLast())
                .build();
    }
}
