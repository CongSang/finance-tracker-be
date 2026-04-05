package com.congsang.financetracker.controller;

import com.congsang.financetracker.dto.response.NotificationResponseDTO;
import com.congsang.financetracker.entity.NotificationEntity;
import com.congsang.financetracker.entity.UserEntity;
import com.congsang.financetracker.service.NotificationService;
import com.congsang.financetracker.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SseService sseService;

    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> getNotifications(@AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(notificationService.getMyNotifications(user));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, @AuthenticationPrincipal UserEntity user) {
        notificationService.markAsRead(id, user);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserEntity user) {
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal UserEntity user) {
        return sseService.subscribe(user.getId());
    }
}