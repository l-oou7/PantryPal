package com.pantrypal.web.dto;

import com.pantrypal.domain.AlertLevel;

import java.time.Instant;
import java.util.List;

public final class AlertDtos {

    private AlertDtos() {}

    public record NotificationDto(Long id, Long ingredientId, String ingredientName, AlertLevel level, int riskScore,
                                  String message, boolean read, Instant createdAt) {}

    public record AlertsDto(List<NotificationDto> notifications, long unread) {}

    public record ScanResult(int created, String message) {}
}
