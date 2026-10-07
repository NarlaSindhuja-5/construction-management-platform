package com.e2e.construction.service;

import com.e2e.construction.dto.NotificationCreateRequest;
import com.e2e.construction.dto.NotificationResponse;
import com.e2e.construction.dto.NotificationSummaryResponse;
import com.e2e.construction.entity.Notification;
import com.e2e.construction.entity.NotificationType;
import com.e2e.construction.entity.Role;
import com.e2e.construction.entity.User;
import com.e2e.construction.exception.BadRequestException;
import com.e2e.construction.exception.ResourceNotFoundException;
import com.e2e.construction.repository.NotificationRepository;
import com.e2e.construction.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    private NotificationService notificationService;

    private User recipient;
    private User otherUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, userRepository);

        Role userRole = new Role("CONTRACTOR", "Contractor role");
        Role adminRole = new Role("ADMIN", "Admin role");

        recipient = new User(userRole, "recipient@test.com", "hash", "John", "Contractor", "1234567890");
        recipient.setId(10L);

        otherUser = new User(userRole, "other@test.com", "hash", "Bob", "Smith", "0987654321");
        otherUser.setId(20L);

        adminUser = new User(adminRole, "admin@test.com", "hash", "Super", "Admin", "1122334455");
        adminUser.setId(99L);
    }

    @Test
    @DisplayName("getNotifications: should return all user notifications")
    void testGetNotifications_All() {
        Notification n1 = new Notification(recipient, "Title 1", "Msg 1", NotificationType.LABOR_WORK_REQUEST);
        n1.setId(1L);
        Notification n2 = new Notification(recipient, "Title 2", "Msg 2", NotificationType.PROJECT_UPDATE);
        n2.setId(2L);

        when(userRepository.findByEmail("recipient@test.com")).thenReturn(Optional.of(recipient));
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(10L)).thenReturn(Arrays.asList(n1, n2));

        List<NotificationResponse> result = notificationService.getNotifications("recipient@test.com", false, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Title 1");
        assertThat(result.get(1).getTitle()).isEqualTo("Title 2");
    }

    @Test
    @DisplayName("getNotifications: should filter by unread only")
    void testGetNotifications_UnreadOnly() {
        Notification n1 = new Notification(recipient, "Unread Title", "Msg", NotificationType.MATERIAL_ORDER);
        n1.setId(1L);

        when(userRepository.findByEmail("recipient@test.com")).thenReturn(Optional.of(recipient));
        when(notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(10L, false)).thenReturn(List.of(n1));

        List<NotificationResponse> result = notificationService.getNotifications("recipient@test.com", true, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isRead()).isFalse();
    }

    @Test
    @DisplayName("getNotifications: should filter by type")
    void testGetNotifications_ByType() {
        Notification n1 = new Notification(recipient, "Tender Title", "Msg", NotificationType.TENDER_UPDATE);
        n1.setId(1L);

        when(userRepository.findByEmail("recipient@test.com")).thenReturn(Optional.of(recipient));
        when(notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(10L, NotificationType.TENDER_UPDATE))
                .thenReturn(List.of(n1));

        List<NotificationResponse> result = notificationService.getNotifications("recipient@test.com", false, NotificationType.TENDER_UPDATE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(NotificationType.TENDER_UPDATE);
    }

    @Test
    @DisplayName("getNotifications: should filter by unread AND type")
    void testGetNotifications_UnreadAndType() {
        Notification n1 = new Notification(recipient, "Unread Machinery", "Msg", NotificationType.MACHINERY_BOOKING_REQUEST);
        n1.setId(1L);

        when(userRepository.findByEmail("recipient@test.com")).thenReturn(Optional.of(recipient));
        when(notificationRepository.findByUserIdAndIsReadAndTypeOrderByCreatedAtDesc(10L, false, NotificationType.MACHINERY_BOOKING_REQUEST))
                .thenReturn(List.of(n1));

        List<NotificationResponse> result = notificationService.getNotifications("recipient@test.com", true, NotificationType.MACHINERY_BOOKING_REQUEST);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(NotificationType.MACHINERY_BOOKING_REQUEST);
    }

    @Test
    @DisplayName("getNotificationById: should return notification when authorized")
    void testGetNotificationById_Success() {
        Notification n = new Notification(recipient, "Title", "Msg", NotificationType.ADMIN_VERIFICATION);
        n.setId(1L);

        when(userRepository.findByEmail("recipient@test.com")).thenReturn(Optional.of(recipient));
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));

        NotificationResponse response = notificationService.getNotificationById(1L, "recipient@test.com");

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Title");
    }

    @Test
    @DisplayName("getNotificationById: should throw AccessDeniedException if belongs to different user")
    void testGetNotificationById_AccessDenied() {
        Notification n = new Notification(recipient, "Title", "Msg", NotificationType.ADMIN_VERIFICATION);
        n.setId(1L);

        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(otherUser));
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));

        assertThrows(AccessDeniedException.class, () ->
                notificationService.getNotificationById(1L, "other@test.com"));
    }

    @Test
    @DisplayName("markAsRead: should mark unread notification as read")
    void testMarkAsRead_Success() {
        Notification n = new Notification(recipient, "Title", "Msg", NotificationType.MATERIAL_ORDER);
        n.setId(5L);
        n.setRead(false);

        when(userRepository.findByEmail("recipient@test.com")).thenReturn(Optional.of(recipient));
        when(notificationRepository.findById(5L)).thenReturn(Optional.of(n));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response = notificationService.markAsRead(5L, "recipient@test.com");

        assertThat(response.isRead()).isTrue();
        assertThat(response.getReadAt()).isNotNull();
        verify(notificationRepository).save(n);
    }

    @Test
    @DisplayName("markAsRead: should be idempotent if already marked read")
    void testMarkAsRead_AlreadyRead() {
        Notification n = new Notification(recipient, "Title", "Msg", NotificationType.MATERIAL_ORDER);
        n.setId(5L);
        n.setRead(true);
        n.setReadAt(LocalDateTime.now().minusHours(1));

        when(userRepository.findByEmail("recipient@test.com")).thenReturn(Optional.of(recipient));
        when(notificationRepository.findById(5L)).thenReturn(Optional.of(n));

        NotificationResponse response = notificationService.markAsRead(5L, "recipient@test.com");

        assertThat(response.isRead()).isTrue();
        verify(notificationRepository, never()).save(n);
    }

    @Test
    @DisplayName("markAllAsRead: should call repository to mark all as read and return count")
    void testMarkAllAsRead_Success() {
        when(userRepository.findByEmail("recipient@test.com")).thenReturn(Optional.of(recipient));
        when(notificationRepository.markAllAsReadForUser(eq(10L), any(LocalDateTime.class))).thenReturn(4);

        int count = notificationService.markAllAsRead("recipient@test.com");

        assertThat(count).isEqualTo(4);
    }

    @Test
    @DisplayName("getNotificationSummary: should return unread count and total count")
    void testGetNotificationSummary() {
        when(userRepository.findByEmail("recipient@test.com")).thenReturn(Optional.of(recipient));
        when(notificationRepository.countByUserIdAndIsReadFalse(10L)).thenReturn(3L);
        when(notificationRepository.countByUserId(10L)).thenReturn(10L);

        NotificationSummaryResponse summary = notificationService.getNotificationSummary("recipient@test.com");

        assertThat(summary.getUnreadCount()).isEqualTo(3L);
        assertThat(summary.getTotalCount()).isEqualTo(10L);
    }

    @Test
    @DisplayName("createNotification: should create from request DTO")
    void testCreateNotification_Success() {
        NotificationCreateRequest request = new NotificationCreateRequest(
                10L, "Custom Title", "Custom Message", NotificationType.PROJECT_UPDATE, 100L, "PROJECT"
        );

        when(userRepository.findById(10L)).thenReturn(Optional.of(recipient));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            n.setId(99L);
            return n;
        });

        NotificationResponse response = notificationService.createNotification(request);

        assertThat(response.getId()).isEqualTo(99L);
        assertThat(response.getTitle()).isEqualTo("Custom Title");
        assertThat(response.getType()).isEqualTo(NotificationType.PROJECT_UPDATE);
    }

    @Test
    @DisplayName("createNotification: should throw BadRequestException if both userId and userEmail missing")
    void testCreateNotification_MissingUser() {
        NotificationCreateRequest request = new NotificationCreateRequest(
                null, "Title", "Message", NotificationType.PROJECT_UPDATE
        );

        assertThrows(BadRequestException.class, () ->
                notificationService.createNotification(request));
    }

    // =========================================================================
    // Test All 12 Event Trigger Methods
    // =========================================================================

    @Test
    @DisplayName("1. notifyLaborWorkRequest creates LABOR_WORK_REQUEST notification")
    void testNotifyLaborWorkRequest() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.notifyLaborWorkRequest(recipient, 101L, "Apex Builders", "Masonry work");

        assertThat(result.getType()).isEqualTo(NotificationType.LABOR_WORK_REQUEST);
        assertThat(result.getTitle()).isEqualTo("New Labor Work Request Received");
        assertThat(result.getMessage()).contains("Apex Builders").contains("Masonry work");
        assertThat(result.getReferenceId()).isEqualTo(101L);
    }

    @Test
    @DisplayName("2. notifyLaborRequestAcceptance creates LABOR_REQUEST_ACCEPTANCE notification")
    void testNotifyLaborRequestAcceptance() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.notifyLaborRequestAcceptance(recipient, 101L, "Ravi Kumar");

        assertThat(result.getType()).isEqualTo(NotificationType.LABOR_REQUEST_ACCEPTANCE);
        assertThat(result.getTitle()).isEqualTo("Labor Work Request Accepted");
        assertThat(result.getMessage()).contains("Ravi Kumar").contains("101");
    }

    @Test
    @DisplayName("3. notifyLaborRequestRejection creates LABOR_REQUEST_REJECTION notification")
    void testNotifyLaborRequestRejection() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.notifyLaborRequestRejection(recipient, 101L, "Ravi Kumar", "Schedule conflict");

        assertThat(result.getType()).isEqualTo(NotificationType.LABOR_REQUEST_REJECTION);
        assertThat(result.getTitle()).isEqualTo("Labor Work Request Declined");
        assertThat(result.getMessage()).contains("Ravi Kumar").contains("Schedule conflict");
    }

    @Test
    @DisplayName("4. notifyMachineryBookingRequest creates MACHINERY_BOOKING_REQUEST notification")
    void testNotifyMachineryBookingRequest() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.notifyMachineryBookingRequest(recipient, 201L, "Caterpillar Excavator", "Apex Builders");

        assertThat(result.getType()).isEqualTo(NotificationType.MACHINERY_BOOKING_REQUEST);
        assertThat(result.getTitle()).isEqualTo("New Machinery Booking Request");
        assertThat(result.getMessage()).contains("Apex Builders").contains("Caterpillar Excavator");
    }

    @Test
    @DisplayName("5. notifyMachineryBookingAcceptance creates MACHINERY_BOOKING_ACCEPTANCE notification")
    void testNotifyMachineryBookingAcceptance() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.notifyMachineryBookingAcceptance(recipient, 201L, "Caterpillar Excavator");

        assertThat(result.getType()).isEqualTo(NotificationType.MACHINERY_BOOKING_ACCEPTANCE);
        assertThat(result.getTitle()).isEqualTo("Machinery Booking Approved");
        assertThat(result.getMessage()).contains("Caterpillar Excavator").contains("201");
    }

    @Test
    @DisplayName("6. notifyMachineryBookingRejection creates MACHINERY_BOOKING_REJECTION notification")
    void testNotifyMachineryBookingRejection() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.notifyMachineryBookingRejection(recipient, 201L, "Caterpillar Excavator", "Maintenance scheduled");

        assertThat(result.getType()).isEqualTo(NotificationType.MACHINERY_BOOKING_REJECTION);
        assertThat(result.getTitle()).isEqualTo("Machinery Booking Declined");
        assertThat(result.getMessage()).contains("Caterpillar Excavator").contains("Maintenance scheduled");
    }

    @Test
    @DisplayName("7. notifyMaterialOrder creates MATERIAL_ORDER notification")
    void testNotifyMaterialOrder() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.notifyMaterialOrder(recipient, 301L, "Portland Cement", "Apex Builders");

        assertThat(result.getType()).isEqualTo(NotificationType.MATERIAL_ORDER);
        assertThat(result.getTitle()).isEqualTo("New Material Order Placed");
        assertThat(result.getMessage()).contains("Apex Builders").contains("Portland Cement");
    }

    @Test
    @DisplayName("8. notifyMaterialOrderAcceptance creates MATERIAL_ORDER_ACCEPTANCE notification")
    void testNotifyMaterialOrderAcceptance() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.notifyMaterialOrderAcceptance(recipient, 301L, "Portland Cement");

        assertThat(result.getType()).isEqualTo(NotificationType.MATERIAL_ORDER_ACCEPTANCE);
        assertThat(result.getTitle()).isEqualTo("Material Order Accepted");
        assertThat(result.getMessage()).contains("Portland Cement").contains("301");
    }

    @Test
    @DisplayName("9. notifyMaterialOrderRejection creates MATERIAL_ORDER_REJECTION notification")
    void testNotifyMaterialOrderRejection() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.notifyMaterialOrderRejection(recipient, 301L, "Portland Cement", "Out of stock");

        assertThat(result.getType()).isEqualTo(NotificationType.MATERIAL_ORDER_REJECTION);
        assertThat(result.getTitle()).isEqualTo("Material Order Declined");
        assertThat(result.getMessage()).contains("Portland Cement").contains("Out of stock");
    }

    @Test
    @DisplayName("10. notifyTenderUpdate creates TENDER_UPDATE notification")
    void testNotifyTenderUpdate() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.notifyTenderUpdate(recipient, 401L, "TND-2026-001", "Closing date extended to next week");

        assertThat(result.getType()).isEqualTo(NotificationType.TENDER_UPDATE);
        assertThat(result.getTitle()).contains("TND-2026-001");
        assertThat(result.getMessage()).contains("Closing date extended to next week");
    }

    @Test
    @DisplayName("11. notifyAdminVerification creates ADMIN_VERIFICATION notification for approved and rejected")
    void testNotifyAdminVerification() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification approved = notificationService.notifyAdminVerification(recipient, "Contractor", true, null);
        assertThat(approved.getType()).isEqualTo(NotificationType.ADMIN_VERIFICATION);
        assertThat(approved.getTitle()).isEqualTo("Account Profile Verified");
        assertThat(approved.getMessage()).contains("successfully verified");

        Notification rejected = notificationService.notifyAdminVerification(recipient, "Laborer", false, "Incomplete KYC documents");
        assertThat(rejected.getType()).isEqualTo(NotificationType.ADMIN_VERIFICATION);
        assertThat(rejected.getTitle()).isEqualTo("Verification Rejected");
        assertThat(rejected.getMessage()).contains("Incomplete KYC documents");
    }

    @Test
    @DisplayName("12. notifyProjectUpdate creates PROJECT_UPDATE notification")
    void testNotifyProjectUpdate() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.notifyProjectUpdate(recipient, 501L, "Metro Station Alpha", "Foundation phase 100% completed");

        assertThat(result.getType()).isEqualTo(NotificationType.PROJECT_UPDATE);
        assertThat(result.getTitle()).contains("Metro Station Alpha");
        assertThat(result.getMessage()).contains("Foundation phase 100% completed");
    }
}
