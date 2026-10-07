package com.e2e.construction.service;

import com.e2e.construction.dto.NotificationCreateRequest;
import com.e2e.construction.dto.NotificationResponse;
import com.e2e.construction.dto.NotificationSummaryResponse;
import com.e2e.construction.entity.Notification;
import com.e2e.construction.entity.NotificationType;
import com.e2e.construction.entity.User;
import com.e2e.construction.exception.BadRequestException;
import com.e2e.construction.exception.ResourceNotFoundException;
import com.e2e.construction.repository.NotificationRepository;
import com.e2e.construction.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get all notifications for the authenticated user, with optional filters for unread status and type.
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(String userEmail, Boolean unreadOnly, NotificationType type) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        List<Notification> notifications;

        boolean filterUnread = Boolean.TRUE.equals(unreadOnly);

        if (filterUnread && type != null) {
            notifications = notificationRepository.findByUserIdAndIsReadAndTypeOrderByCreatedAtDesc(user.getId(), false, type);
        } else if (filterUnread) {
            notifications = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(user.getId(), false);
        } else if (type != null) {
            notifications = notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(user.getId(), type);
        } else {
            notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        }

        if (notifications == null) {
            return Collections.emptyList();
        }

        return notifications.stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get a single notification by ID, verifying authorization.
     */
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        boolean isAdmin = user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getName());
        if (!notification.getUser().getId().equals(user.getId()) && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to view this notification.");
        }

        return NotificationResponse.fromEntity(notification);
    }

    /**
     * Mark a single notification as read.
     */
    @Transactional
    public NotificationResponse markAsRead(Long id, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        boolean isAdmin = user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getName());
        if (!notification.getUser().getId().equals(user.getId()) && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to modify this notification.");
        }

        if (!notification.isRead()) {
            notification.markAsRead();
            notification = notificationRepository.save(notification);
            log.info("Marked notification {} as read for user {}", id, userEmail);
        }

        return NotificationResponse.fromEntity(notification);
    }

    /**
     * Mark all unread notifications as read for the authenticated user.
     */
    @Transactional
    public int markAllAsRead(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        int updatedCount = notificationRepository.markAllAsReadForUser(user.getId(), LocalDateTime.now());
        log.info("Marked {} notifications as read for user {}", updatedCount, userEmail);
        return updatedCount;
    }

    /**
     * Get notification summary (unread count and total count) for the authenticated user.
     */
    @Transactional(readOnly = true)
    public NotificationSummaryResponse getNotificationSummary(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        long unread = notificationRepository.countByUserIdAndIsReadFalse(user.getId());
        long total = notificationRepository.countByUserId(user.getId());

        return new NotificationSummaryResponse(unread, total);
    }

    /**
     * Create and send a notification from request DTO.
     */
    @Transactional
    public NotificationResponse createNotification(NotificationCreateRequest request) {
        User recipient;
        if (request.getUserId() != null) {
            recipient = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));
        } else if (request.getUserEmail() != null && !request.getUserEmail().isBlank()) {
            recipient = userRepository.findByEmail(request.getUserEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getUserEmail()));
        } else {
            throw new BadRequestException("Either userId or userEmail must be provided");
        }

        Notification notification = new Notification(
                recipient,
                request.getTitle(),
                request.getMessage(),
                request.getType(),
                request.getReferenceId(),
                request.getReferenceType()
        );

        Notification saved = notificationRepository.save(notification);
        log.info("Created notification ID: {} of type: {} for user: {}", saved.getId(), saved.getType(), recipient.getEmail());
        return NotificationResponse.fromEntity(saved);
    }

    /**
     * General notification creator helper.
     */
    @Transactional
    public Notification sendNotification(User recipient, String title, String message, NotificationType type, Long referenceId, String referenceType) {
        if (recipient == null) {
            log.warn("Cannot send notification: recipient is null");
            return null;
        }

        Notification notification = new Notification(recipient, title, message, type, referenceId, referenceType);
        Notification saved = notificationRepository.save(notification);
        if (saved != null) {
            log.info("Sent notification ID: {} [{}] to user: {}", saved.getId(), type, recipient.getEmail());
        }
        return saved;
    }

    // =========================================================================
    // 12 Dedicated Trigger Methods
    // =========================================================================

    /**
     * 1. Labor work request notification
     */
    @Transactional
    public Notification notifyLaborWorkRequest(User recipient, Long requestId, String contractorName, String jobDescription) {
        String title = "New Labor Work Request Received";
        String message = String.format("Contractor %s has requested your services for: %s (Request #%d).",
                contractorName != null ? contractorName : "A contractor",
                jobDescription != null ? jobDescription : "General Labor",
                requestId);
        return sendNotification(recipient, title, message, NotificationType.LABOR_WORK_REQUEST, requestId, "LABOR_REQUEST");
    }

    /**
     * 2. Labor request acceptance notification
     */
    @Transactional
    public Notification notifyLaborRequestAcceptance(User recipient, Long requestId, String laborerName) {
        String title = "Labor Work Request Accepted";
        String message = String.format("%s has accepted your work request (Request #%d).",
                laborerName != null ? laborerName : "The laborer",
                requestId);
        return sendNotification(recipient, title, message, NotificationType.LABOR_REQUEST_ACCEPTANCE, requestId, "LABOR_REQUEST");
    }

    /**
     * 3. Labor request rejection notification
     */
    @Transactional
    public Notification notifyLaborRequestRejection(User recipient, Long requestId, String laborerName, String reason) {
        String title = "Labor Work Request Declined";
        String message = String.format("%s has declined your work request (Request #%d).%s",
                laborerName != null ? laborerName : "The laborer",
                requestId,
                reason != null && !reason.isBlank() ? " Reason: " + reason : "");
        return sendNotification(recipient, title, message, NotificationType.LABOR_REQUEST_REJECTION, requestId, "LABOR_REQUEST");
    }

    /**
     * 4. Machinery booking request notification
     */
    @Transactional
    public Notification notifyMachineryBookingRequest(User recipient, Long bookingId, String machineName, String contractorName) {
        String title = "New Machinery Booking Request";
        String message = String.format("%s requested to book your machinery '%s' (Booking #%d).",
                contractorName != null ? contractorName : "A contractor",
                machineName != null ? machineName : "Machinery",
                bookingId);
        return sendNotification(recipient, title, message, NotificationType.MACHINERY_BOOKING_REQUEST, bookingId, "MACHINERY_BOOKING");
    }

    /**
     * 5. Machinery booking acceptance notification
     */
    @Transactional
    public Notification notifyMachineryBookingAcceptance(User recipient, Long bookingId, String machineName) {
        String title = "Machinery Booking Approved";
        String message = String.format("Your booking request for machinery '%s' has been accepted (Booking #%d).",
                machineName != null ? machineName : "Machinery",
                bookingId);
        return sendNotification(recipient, title, message, NotificationType.MACHINERY_BOOKING_ACCEPTANCE, bookingId, "MACHINERY_BOOKING");
    }

    /**
     * 6. Machinery booking rejection notification
     */
    @Transactional
    public Notification notifyMachineryBookingRejection(User recipient, Long bookingId, String machineName, String reason) {
        String title = "Machinery Booking Declined";
        String message = String.format("Your booking request for machinery '%s' was declined (Booking #%d).%s",
                machineName != null ? machineName : "Machinery",
                bookingId,
                reason != null && !reason.isBlank() ? " Reason: " + reason : "");
        return sendNotification(recipient, title, message, NotificationType.MACHINERY_BOOKING_REJECTION, bookingId, "MACHINERY_BOOKING");
    }

    /**
     * 7. Material order notification
     */
    @Transactional
    public Notification notifyMaterialOrder(User recipient, Long orderId, String materialName, String contractorName) {
        String title = "New Material Order Placed";
        String message = String.format("%s placed a new order for material '%s' (Order #%d).",
                contractorName != null ? contractorName : "A contractor",
                materialName != null ? materialName : "Material",
                orderId);
        return sendNotification(recipient, title, message, NotificationType.MATERIAL_ORDER, orderId, "MATERIAL_ORDER");
    }

    /**
     * 8. Material order acceptance notification
     */
    @Transactional
    public Notification notifyMaterialOrderAcceptance(User recipient, Long orderId, String materialName) {
        String title = "Material Order Accepted";
        String message = String.format("Your order for material '%s' (Order #%d) has been accepted by the supplier.",
                materialName != null ? materialName : "Material",
                orderId);
        return sendNotification(recipient, title, message, NotificationType.MATERIAL_ORDER_ACCEPTANCE, orderId, "MATERIAL_ORDER");
    }

    /**
     * 9. Material order rejection notification
     */
    @Transactional
    public Notification notifyMaterialOrderRejection(User recipient, Long orderId, String materialName, String reason) {
        String title = "Material Order Declined";
        String message = String.format("Your order for material '%s' (Order #%d) was declined by the supplier.%s",
                materialName != null ? materialName : "Material",
                orderId,
                reason != null && !reason.isBlank() ? " Reason: " + reason : "");
        return sendNotification(recipient, title, message, NotificationType.MATERIAL_ORDER_REJECTION, orderId, "MATERIAL_ORDER");
    }

    /**
     * 10. Tender updates notification
     */
    @Transactional
    public Notification notifyTenderUpdate(User recipient, Long tenderId, String tenderNumber, String updateDetails) {
        String title = String.format("Tender Update: %s", tenderNumber != null ? tenderNumber : "Tender");
        String message = String.format("Tender %s has been updated: %s",
                tenderNumber != null ? tenderNumber : "#" + tenderId,
                updateDetails != null ? updateDetails : "Status or requirements have changed.");
        return sendNotification(recipient, title, message, NotificationType.TENDER_UPDATE, tenderId, "TENDER");
    }

    /**
     * 11. Admin verification notification
     */
    @Transactional
    public Notification notifyAdminVerification(User recipient, String entityType, boolean approved, String remarks) {
        String title = approved ? "Account Profile Verified" : "Verification Rejected";
        String message = approved
                ? String.format("Your %s profile has been successfully verified by Administrator.", entityType != null ? entityType : "account")
                : String.format("Your %s verification was rejected by Administrator.%s",
                entityType != null ? entityType : "account",
                remarks != null && !remarks.isBlank() ? " Remarks: " + remarks : "");
        return sendNotification(recipient, title, message, NotificationType.ADMIN_VERIFICATION, null, "VERIFICATION");
    }

    /**
     * 12. Project updates notification
     */
    @Transactional
    public Notification notifyProjectUpdate(User recipient, Long projectId, String projectName, String updateDetails) {
        String title = String.format("Project Update: %s", projectName != null ? projectName : "Project");
        String message = String.format("Project '%s' update: %s",
                projectName != null ? projectName : "#" + projectId,
                updateDetails != null ? updateDetails : "Progress or details have been updated.");
        return sendNotification(recipient, title, message, NotificationType.PROJECT_UPDATE, projectId, "PROJECT");
    }
}
