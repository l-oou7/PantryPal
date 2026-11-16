package com.pantrypal.web;

import com.pantrypal.security.PantryUserDetails;
import com.pantrypal.service.ExpiryMonitorService;
import com.pantrypal.service.NotificationService;
import com.pantrypal.web.dto.AlertDtos;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final NotificationService notifications;
    private final ExpiryMonitorService monitor;

    public AlertController(NotificationService notifications, ExpiryMonitorService monitor) {
        this.notifications = notifications;
        this.monitor = monitor;
    }

    @GetMapping
    public AlertDtos.AlertsDto list(@AuthenticationPrincipal PantryUserDetails me) {
        return notifications.list(me.getId());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unread(@AuthenticationPrincipal PantryUserDetails me) {
        return Map.of("unread", notifications.unread(me.getId()));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> read(@AuthenticationPrincipal PantryUserDetails me, @PathVariable Long id) {
        notifications.markRead(me.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> readAll(@AuthenticationPrincipal PantryUserDetails me) {
        notifications.markAllRead(me.getId());
        return ResponseEntity.noContent().build();
    }

    /** Runs the same scan the scheduler runs, for this user only. */
    @PostMapping("/scan")
    public AlertDtos.ScanResult scan(@AuthenticationPrincipal PantryUserDetails me) {
        int n = monitor.scanUser(me.getId());
        return new AlertDtos.ScanResult(n, n == 0 ? "All checked! No new alerts." : n + " new alert" + (n == 1 ? "" : "s") + " found.");
    }
}
