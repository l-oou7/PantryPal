package com.pantrypal.service;

import com.pantrypal.model.Notification;
import com.pantrypal.repository.NotificationRepository;
import com.pantrypal.web.ApiException;
import com.pantrypal.web.dto.AlertDtos;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notifications;

    public NotificationService(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public AlertDtos.AlertsDto list(Long userId) {
        return new AlertDtos.AlertsDto(
                notifications.findTop50ByUserIdOrderByCreatedAtDesc(userId).stream().map(NotificationService::toDto).toList(),
                notifications.countByUserIdAndReadFalse(userId));
    }

    @Transactional(readOnly = true)
    public long unread(Long userId) {
        return notifications.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markRead(Long userId, Long id) {
        notifications.findByIdAndUserId(id, userId).orElseThrow(() -> ApiException.notFound("Notification")).setRead(true);
    }

    @Transactional
    public void markAllRead(Long userId) {
        notifications.markAllRead(userId);
    }

    static AlertDtos.NotificationDto toDto(Notification n) {
        return new AlertDtos.NotificationDto(n.getId(), n.getIngredientId(), n.getIngredientName(), n.getLevel(),
                n.getRiskScore(), n.getMessage(), n.isRead(), n.getCreatedAt());
    }
}
