package com.siupo.restaurant.service.notifications;

import com.siupo.restaurant.dto.request.CreateNotificationRequest;
import com.siupo.restaurant.dto.response.NotificationResponse;
import com.siupo.restaurant.enums.ENotificationStatus;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.model.UserNotification;
import com.siupo.restaurant.repository.UserNotificationRepository;
import com.siupo.restaurant.repository.UserRepository;
import com.siupo.restaurant.service.notifications.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserNotificationServiceImpl implements UserNotificationService {

    private final UserNotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotificationsByUser(Long userId) {
        List<UserNotification> notifications = notificationRepository
                .findByUserIdAndStatusNotOrderBySentAtDesc(userId, ENotificationStatus.DELETED);

        return notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        UserNotification notification = notificationRepository
                .findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new RuntimeException("Notification not found or access denied"));

        if (notification.getStatus() == ENotificationStatus.DELETED) {
            throw new RuntimeException("Cannot read deleted notification");
        }

        notification.setStatus(ENotificationStatus.READ);
        UserNotification updated = notificationRepository.save(notification);

        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        UserNotification notification = notificationRepository
                .findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new RuntimeException("Notification not found or access denied"));

        notification.setStatus(ENotificationStatus.DELETED);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotifications() {
        List<UserNotification> notifications = notificationRepository.findAllByOrderBySentAtDesc();

        return notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<NotificationResponse> createNotification(CreateNotificationRequest request) {
        List<UserNotification> notifications;

        // Nếu sendToAll = true hoặc userId = null thì gửi cho tất cả user
        if (Boolean.TRUE.equals(request.getSendToAll()) || request.getUserId() == null) {
            List<User> allUsers = userRepository.findAll();

            if (allUsers.isEmpty()) {
                throw new RuntimeException("No users found");
            }

            notifications = allUsers.stream()
                    .map(user -> UserNotification.builder()
                            .title(request.getTitle())
                            .content(request.getContent())
                            .sentAt(Instant.now())
                            .status(ENotificationStatus.UNREAD)
                            .user(user)
                            .build())
                    .collect(Collectors.toList());

            notifications = notificationRepository.saveAll(notifications);
        } else {
            // Gửi cho 1 user cụ thể
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UserNotification notification = UserNotification.builder()
                    .title(request.getTitle())
                    .content(request.getContent())
                    .sentAt(Instant.now())
                    .status(ENotificationStatus.UNREAD)
                    .user(user)
                    .build();

            notifications = List.of(notificationRepository.save(notification));
        }

        return notifications.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private NotificationResponse convertToResponse(UserNotification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .sentAt(notification.getSentAt())
                .title(notification.getTitle())
                .content(notification.getContent())
                .status(notification.getStatus())
                .userId(notification.getUser().getId())
                .build();
    }
}