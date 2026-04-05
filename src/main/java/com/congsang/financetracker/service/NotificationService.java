package com.congsang.financetracker.service;


import com.congsang.financetracker.common.enums.Notification;
import com.congsang.financetracker.dto.response.NotificationResponseDTO;
import com.congsang.financetracker.entity.NotificationEntity;
import com.congsang.financetracker.entity.UserEntity;
import com.congsang.financetracker.exception.ResourceNotFoundException;
import com.congsang.financetracker.mapper.NotificationMapper;
import com.congsang.financetracker.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SseService sseService;
    private final NotificationMapper notificationMapper;

    public void createBudgetNotification(UserEntity user, String categoryName, double percentUsed) {
        String title;
        String message;
        Notification type;

        if (percentUsed >= 100) {
            title = "🚨 Vượt định mức chi tiêu!";
            message = String.format("Bạn đã tiêu quá ngân sách cho mục '%s' (%s%%). Hãy thắt chặt chi tiêu nhé!",
                    categoryName, Math.round(percentUsed));
            type = Notification.BUDGET_EXCEEDED;
        } else if (percentUsed >= 80) {
            title = "⚠️ Sắp chạm hạn mức";
            message = String.format("Ngân sách cho '%s' đã dùng hết %s%%. Bạn nên cân nhắc trước khi chi thêm.",
                    categoryName, Math.round(percentUsed));
            type = Notification.BUDGET_WARNING;
        } else {
            return; // Không cần thông báo nếu dưới 80%
        }

        NotificationEntity notification = new NotificationEntity();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setUser(user);
        notificationRepository.save(notification);

        sseService.sendNotification(user.getId(), notification);
    }

    // Lấy danh sách thông báo cho chuông thông báo
    public List<NotificationResponseDTO> getMyNotifications(UserEntity user) {
        List<NotificationEntity> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        return notifications.stream()
                .map(notificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long id, UserEntity user) {
        NotificationEntity notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo"));

        if (notification.getUser().getId().equals(user.getId())) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllAsRead(UserEntity user) {
        notificationRepository.markAllAsReadByUser(user);
    }
}
