package com.siupo.restaurant.service.notifications;

import com.siupo.restaurant.dto.request.CreateNotificationRequest;
import com.siupo.restaurant.dto.response.NotificationResponse;
import com.siupo.restaurant.enums.ENotificationStatus;
import com.siupo.restaurant.model.NotificationReadStatus;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.model.UserNotification;
import com.siupo.restaurant.repository.NotificationReadStatusRepository;
import com.siupo.restaurant.repository.UserNotificationRepository;
import com.siupo.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserNotificationServiceImpl implements UserNotificationService {

    private final UserNotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationReadStatusRepository readStatusRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotificationsByUser(Long userId) {
        // 1. Lấy personal notifications (loại bỏ DELETED)
        List<UserNotification> personalNotifications = notificationRepository
                .findByUserIdAndStatusNotOrderBySentAtDesc(userId, ENotificationStatus.DELETED);

        // 2. Lấy tất cả global notifications
        List<UserNotification> globalNotifications = notificationRepository
                .findByIsGlobalTrueOrderBySentAtDesc();

        // 3. Lấy status map của user cho global notifications
        Map<Long, ENotificationStatus> statusMap = readStatusRepository
                .findStatusMapByUserId(userId)
                .stream()
                .collect(Collectors.toMap(
                        NotificationReadStatusRepository.NotificationStatusProjection::getNotificationId,
                        NotificationReadStatusRepository.NotificationStatusProjection::getStatus
                ));

        List<NotificationResponse> responses = new ArrayList<>();

        // 4. Add personal notifications
        personalNotifications.forEach(notif -> {
            responses.add(convertToResponse(notif));
        });

        // 5. Add global notifications (loại bỏ những cái đã DELETED)
        globalNotifications.forEach(notif -> {
            ENotificationStatus userStatus = statusMap.getOrDefault(notif.getId(), ENotificationStatus.UNREAD);

            // Chỉ add nếu user chưa delete
            if (userStatus != ENotificationStatus.DELETED) {
                NotificationResponse response = NotificationResponse.builder()
                        .id(notif.getId())
                        .sentAt(notif.getSentAt())
                        .title(notif.getTitle())
                        .content(notif.getContent())
                        .status(userStatus) // READ hoặc UNREAD
                        .userId(null)
                        .isGlobal(true)
                        .build();
                responses.add(response);
            }
        });

        // 6. Sort theo thời gian
        responses.sort(Comparator.comparing(NotificationResponse::getSentAt).reversed());

        return responses;
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        UserNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (notification.getIsGlobal()) {
            // Global notification: lưu vào tracking table
            NotificationReadStatus readStatus = readStatusRepository
                    .findByNotificationIdAndUserId(notificationId, userId)
                    .orElse(NotificationReadStatus.builder()
                            .notification(notification)
                            .user(userRepository.findById(userId)
                                    .orElseThrow(() -> new RuntimeException("User not found")))
                            .build());

            if (readStatus.getStatus() == ENotificationStatus.DELETED) {
                throw new RuntimeException("Cannot read deleted notification");
            }

            readStatus.setStatus(ENotificationStatus.READ);
            readStatus.setUpdatedAt(Instant.now());
            readStatusRepository.save(readStatus);

            return NotificationResponse.builder()
                    .id(notification.getId())
                    .sentAt(notification.getSentAt())
                    .title(notification.getTitle())
                    .content(notification.getContent())
                    .status(ENotificationStatus.READ)
                    .userId(null)
                    .isGlobal(true)
                    .build();
        } else {
            // Personal notification: update trực tiếp
            UserNotification personalNotif = notificationRepository
                    .findByIdAndUserId(notificationId, userId)
                    .orElseThrow(() -> new RuntimeException("Notification not found or access denied"));

            if (personalNotif.getStatus() == ENotificationStatus.DELETED) {
                throw new RuntimeException("Cannot read deleted notification");
            }

            personalNotif.setStatus(ENotificationStatus.READ);
            UserNotification updated = notificationRepository.save(personalNotif);

            return convertToResponse(updated);
        }
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        UserNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (notification.getIsGlobal()) {
            // Global notification: đánh dấu DELETED trong tracking table
            NotificationReadStatus readStatus = readStatusRepository
                    .findByNotificationIdAndUserId(notificationId, userId)
                    .orElse(NotificationReadStatus.builder()
                            .notification(notification)
                            .user(userRepository.findById(userId)
                                    .orElseThrow(() -> new RuntimeException("User not found")))
                            .build());

            readStatus.setStatus(ENotificationStatus.DELETED);
            readStatus.setUpdatedAt(Instant.now());
            readStatusRepository.save(readStatus);
        } else {
            // Personal notification: update trực tiếp
            UserNotification personalNotif = notificationRepository
                    .findByIdAndUserId(notificationId, userId)
                    .orElseThrow(() -> new RuntimeException("Notification not found or access denied"));

            personalNotif.setStatus(ENotificationStatus.DELETED);
            notificationRepository.save(personalNotif);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotifications() {
        List<UserNotification> notifications = notificationRepository.findAllByOrderBySentAtDesc();

        return notifications.stream()
                .map(notif -> {
                    if (notif.getIsGlobal()) {
                        return NotificationResponse.builder()
                                .id(notif.getId())
                                .sentAt(notif.getSentAt())
                                .title(notif.getTitle())
                                .content(notif.getContent())
                                .status(null) // Admin không cần status
                                .userId(null)
                                .isGlobal(true)
                                .build();
                    } else {
                        return convertToResponse(notif);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<NotificationResponse> createNotification(CreateNotificationRequest request) {
        UserNotification notification;

        if (Boolean.TRUE.equals(request.getSendToAll()) || request.getUserId() == null) {
            // Tạo 1 notification chung cho tất cả
            notification = UserNotification.builder()
                    .title(request.getTitle())
                    .content(request.getContent())
                    .sentAt(Instant.now())
                    .user(null) // Không gán user cụ thể
                    .isGlobal(true)
                    .status(null) // Không dùng status cho global
                    .build();

            notification = notificationRepository.save(notification);

            return List.of(NotificationResponse.builder()
                    .id(notification.getId())
                    .sentAt(notification.getSentAt())
                    .title(notification.getTitle())
                    .content(notification.getContent())
                    .status(null)
                    .userId(null)
                    .isGlobal(true)
                    .build());
        } else {
            // Gửi cho 1 user cụ thể
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            notification = UserNotification.builder()
                    .title(request.getTitle())
                    .content(request.getContent())
                    .sentAt(Instant.now())
                    .status(ENotificationStatus.UNREAD)
                    .user(user)
                    .isGlobal(false)
                    .build();

            notification = notificationRepository.save(notification);

            return List.of(convertToResponse(notification));
        }
    }

    private NotificationResponse convertToResponse(UserNotification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .sentAt(notification.getSentAt())
                .title(notification.getTitle())
                .content(notification.getContent())
                .status(notification.getStatus())
                .userId(notification.getUser() != null ? notification.getUser().getId() : null)
                .isGlobal(notification.getIsGlobal())
                .build();
    }
}