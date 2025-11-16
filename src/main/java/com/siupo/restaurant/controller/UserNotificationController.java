package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.request.CreateNotificationRequest;
import com.siupo.restaurant.dto.response.NotificationResponse;
import com.siupo.restaurant.model.Customer;
import com.siupo.restaurant.service.notifications.UserNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class UserNotificationController {

    private final UserNotificationService notificationService;

    // ==================== CUSTOMER APIs ====================

    @GetMapping("/customer")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal Customer customer) {

        log.info("Getting notifications for customer: {}", customer.getId());

        List<NotificationResponse> notifications =
                notificationService.getAllNotificationsByUser(customer.getId());

        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/customer/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal Customer customer) {  // ✅ Đổi từ Principal sang Customer

        log.info("Customer {} marking notification {} as read", customer.getId(), id);

        NotificationResponse notification = notificationService.markAsRead(id, customer.getId());
        return ResponseEntity.ok(notification);
    }

    @DeleteMapping("/customer/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal Customer customer) {  // ✅ Đổi từ Principal sang Customer

        log.info("Customer {} deleting notification {}", customer.getId(), id);

        notificationService.deleteNotification(id, customer.getId());
        return ResponseEntity.noContent().build();
    }

    // ==================== ADMIN APIs ====================

    @GetMapping("/admin")
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
        List<NotificationResponse> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/admin")
    public ResponseEntity<List<NotificationResponse>> createNotification(
            @Valid @RequestBody CreateNotificationRequest request) {
        List<NotificationResponse> notifications = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(notifications);
    }

}