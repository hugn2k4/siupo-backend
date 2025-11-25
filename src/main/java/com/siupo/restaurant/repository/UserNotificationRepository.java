package com.siupo.restaurant.repository;

import com.siupo.restaurant.enums.ENotificationStatus;
import com.siupo.restaurant.model.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    List<UserNotification> findByUserIdAndStatusNotOrderBySentAtDesc(Long userId, ENotificationStatus status);

    Optional<UserNotification> findByIdAndUserId(Long id, Long userId);

    List<UserNotification> findAllByOrderBySentAtDesc();

    List<UserNotification> findByIsGlobalTrueOrderBySentAtDesc();

    // Lấy personal notifications của user
    @Query("SELECT n FROM UserNotification n WHERE n.user.id = :userId AND n.isGlobal = false ORDER BY n.sentAt DESC")
    List<UserNotification> findPersonalNotificationsByUserId(@Param("userId") Long userId);
}