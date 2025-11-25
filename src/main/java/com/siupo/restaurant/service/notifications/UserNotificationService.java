package com.siupo.restaurant.service.notifications;

import com.siupo.restaurant.dto.request.CreateNotificationRequest;
import com.siupo.restaurant.dto.response.NotificationResponse;

import java.util.List;

public interface UserNotificationService {

    // Customer APIs
    List<NotificationResponse> getAllNotificationsByUser(Long userId);

    NotificationResponse markAsRead(Long notificationId, Long userId);

    void deleteNotification(Long notificationId, Long userId);

    // Admin APIs
    List<NotificationResponse> getAllNotifications();

    // Tạo thông báo cho 1 user hoặc tất cả user
    List<NotificationResponse> createNotification(CreateNotificationRequest request);
}