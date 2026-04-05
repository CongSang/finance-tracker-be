package com.congsang.financetracker.dto.response;

import com.congsang.financetracker.common.enums.Notification;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationResponseDTO {

    private Long id;
    private String title;
    private String message;
    private Notification type;
    private boolean isRead;
}
