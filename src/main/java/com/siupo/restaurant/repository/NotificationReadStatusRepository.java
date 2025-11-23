package com.siupo.restaurant.repository;

import com.siupo.restaurant.enums.ENotificationStatus;
import com.siupo.restaurant.model.NotificationReadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;

@Repository
public interface NotificationReadStatusRepository extends JpaRepository<NotificationReadStatus, Long> {

    Optional<NotificationReadStatus> findByNotificationIdAndUserId(Long notificationId, Long userId);

    boolean existsByNotificationIdAndUserId(Long notificationId, Long userId);

    // Lấy map notification_id -> status cho user
    @Query("SELECT nrs.notification.id as notificationId, nrs.status as status " +
            "FROM NotificationReadStatus nrs " +
            "WHERE nrs.user.id = :userId")
    java.util.List<NotificationStatusProjection> findStatusMapByUserId(@Param("userId") Long userId);

    interface NotificationStatusProjection {
        Long getNotificationId();
        ENotificationStatus getStatus();
    }
}