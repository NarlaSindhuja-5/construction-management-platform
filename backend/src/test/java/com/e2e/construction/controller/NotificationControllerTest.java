package com.e2e.construction.controller;

import com.e2e.construction.dto.NotificationCreateRequest;
import com.e2e.construction.entity.Notification;
import com.e2e.construction.entity.NotificationType;
import com.e2e.construction.entity.Role;
import com.e2e.construction.entity.User;
import com.e2e.construction.exception.GlobalExceptionHandler;
import com.e2e.construction.repository.NotificationRepository;
import com.e2e.construction.repository.UserRepository;
import com.e2e.construction.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    private NotificationService notificationService;
    private NotificationController notificationController;

    private Principal principal;
    private User testUser;
    private Notification sampleNotification;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, userRepository);
        notificationController = new NotificationController(notificationService);

        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Role contractorRole = new Role("CONTRACTOR", "Contractor");
        testUser = new User(contractorRole, "user@test.com", "hash", "John", "Doe", "1234567890");
        testUser.setId(1L);

        sampleNotification = new Notification(
                testUser,
                "Labor Request Received",
                "You have a new work request",
                NotificationType.LABOR_WORK_REQUEST,
                100L,
                "LABOR_REQUEST"
        );
        sampleNotification.setId(10L);
        sampleNotification.setCreatedAt(LocalDateTime.now());

        principal = () -> "user@test.com";
    }

    @Test
    @DisplayName("GET /api/notifications - should return list of notifications")
    void testGetNotifications() throws Exception {
        Notification n2 = new Notification(
                testUser,
                "Machinery Booking Accepted",
                "Booking accepted",
                NotificationType.MACHINERY_BOOKING_ACCEPTANCE,
                200L,
                "MACHINERY_BOOKING"
        );
        n2.setId(11L);
        n2.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Arrays.asList(sampleNotification, n2));

        mockMvc.perform(get("/api/notifications")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(10)))
                .andExpect(jsonPath("$[0].title", is("Labor Request Received")))
                .andExpect(jsonPath("$[0].type", is("LABOR_WORK_REQUEST")))
                .andExpect(jsonPath("$[0].read", is(false)))
                .andExpect(jsonPath("$[1].id", is(11)))
                .andExpect(jsonPath("$[1].type", is("MACHINERY_BOOKING_ACCEPTANCE")));
    }

    @Test
    @DisplayName("GET /api/notifications?unreadOnly=true - should return unread notifications")
    void testGetNotifications_UnreadOnly() throws Exception {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(1L, false))
                .thenReturn(List.of(sampleNotification));

        mockMvc.perform(get("/api/notifications")
                        .param("unreadOnly", "true")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(10)))
                .andExpect(jsonPath("$[0].read", is(false)));
    }

    @Test
    @DisplayName("GET /api/notifications?type=MATERIAL_ORDER - should return notifications filtered by type")
    void testGetNotifications_ByType() throws Exception {
        sampleNotification.setType(NotificationType.MATERIAL_ORDER);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(1L, NotificationType.MATERIAL_ORDER))
                .thenReturn(List.of(sampleNotification));

        mockMvc.perform(get("/api/notifications")
                        .param("type", "MATERIAL_ORDER")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type", is("MATERIAL_ORDER")));
    }

    @Test
    @DisplayName("GET /api/notifications/{id} - should return single notification")
    void testGetNotificationById() throws Exception {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(sampleNotification));

        mockMvc.perform(get("/api/notifications/10")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.title", is("Labor Request Received")));
    }

    @Test
    @DisplayName("GET /api/notifications/unread-count - should return unread and total count")
    void testGetUnreadCount() throws Exception {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(5L);
        when(notificationRepository.countByUserId(1L)).thenReturn(12L);

        mockMvc.perform(get("/api/notifications/unread-count")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount", is(5)))
                .andExpect(jsonPath("$.totalCount", is(12)));
    }

    @Test
    @DisplayName("PATCH /api/notifications/{id}/read - should mark notification as read")
    void testMarkAsRead_Patch() throws Exception {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(sampleNotification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(patch("/api/notifications/10/read")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.read", is(true)))
                .andExpect(jsonPath("$.readAt").exists());
    }

    @Test
    @DisplayName("PUT /api/notifications/{id}/read - should also mark notification as read")
    void testMarkAsRead_Put() throws Exception {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(sampleNotification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/notifications/10/read")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.read", is(true)));
    }

    @Test
    @DisplayName("PATCH /api/notifications/mark-all-read - should mark all as read")
    void testMarkAllAsRead_Patch() throws Exception {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(notificationRepository.markAllAsReadForUser(eq(1L), any(LocalDateTime.class))).thenReturn(3);

        mockMvc.perform(patch("/api/notifications/mark-all-read")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("All notifications marked as read")))
                .andExpect(jsonPath("$.markedCount", is(3)));
    }

    @Test
    @DisplayName("PUT /api/notifications/mark-all-read - should also mark all as read")
    void testMarkAllAsRead_Put() throws Exception {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(testUser));
        when(notificationRepository.markAllAsReadForUser(eq(1L), any(LocalDateTime.class))).thenReturn(5);

        mockMvc.perform(put("/api/notifications/mark-all-read")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.markedCount", is(5)));
    }

    @Test
    @DisplayName("POST /api/notifications - should create notification")
    void testCreateNotification() throws Exception {
        NotificationCreateRequest request = new NotificationCreateRequest(
                1L, "Tender Published", "A new tender has been published",
                NotificationType.TENDER_UPDATE, 50L, "TENDER"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification n = inv.getArgument(0);
            n.setId(99L);
            n.setCreatedAt(LocalDateTime.now());
            return n;
        });

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(principal))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(99)))
                .andExpect(jsonPath("$.title", is("Tender Published")))
                .andExpect(jsonPath("$.type", is("TENDER_UPDATE")))
                .andExpect(jsonPath("$.read", is(false)));
    }
}
