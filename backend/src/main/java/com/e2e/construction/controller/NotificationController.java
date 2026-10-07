package com.e2e.construction.controller;

import com.e2e.construction.dto.NotificationCreateRequest;
import com.e2e.construction.dto.NotificationResponse;
import com.e2e.construction.dto.NotificationSummaryResponse;
import com.e2e.construction.entity.NotificationType;
import com.e2e.construction.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * GET /api/notifications
     * Retrieve all notifications for the authenticated user.
     * Supports filtering by unread status and notification type.
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            Principal principal,
            @RequestParam(required = false) Boolean unreadOnly,
            @RequestParam(required = false) NotificationType type) {

        List<NotificationResponse> notifications = notificationService.getNotifications(principal.getName(), unreadOnly, type);
        return ResponseEntity.ok(notifications);
    }

    /**
     * GET /api/notifications/{id}
     * Retrieve a specific notification by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(
            @PathVariable Long id,
            Principal principal) {

        NotificationResponse response = notificationService.getNotificationById(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/notifications/unread-count
     * Quick metric endpoint returning unread and total notification counts.
     */
    @GetMapping("/unread-count")
    public ResponseEntity<NotificationSummaryResponse> getUnreadCount(Principal principal) {
        NotificationSummaryResponse summary = notificationService.getNotificationSummary(principal.getName());
        return ResponseEntity.ok(summary);
    }

    /**
     * PATCH /api/notifications/{id}/read
     * Mark a single notification as read.
     * Also supports PUT /api/notifications/{id}/read.
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            Principal principal) {

        NotificationResponse response = notificationService.markAsRead(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsReadPut(
            @PathVariable Long id,
            Principal principal) {

        return markAsRead(id, principal);
    }

    /**
     * PATCH /api/notifications/mark-all-read
     * Mark all notifications as read for current user.
     * Also supports PUT /api/notifications/mark-all-read.
     */
    @PatchMapping("/mark-all-read")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Principal principal) {
        int markedCount = notificationService.markAllAsRead(principal.getName());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "All notifications marked as read");
        response.put("markedCount", markedCount);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<Map<String, Object>> markAllAsReadPut(Principal principal) {
        return markAllAsRead(principal);
    }

    /**
     * POST /api/notifications
     * Create/send notification (admin broadcast or manual creation).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationResponse> createNotification(
            @Valid @RequestBody NotificationCreateRequest request) {

        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
