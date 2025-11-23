package com.siupo.restaurant.model;

import jakarta.persistence.*;
import lombok.*;
import com.siupo.restaurant.enums.ENotificationStatus;

import java.time.Instant;

@Entity
@Table(name = "notification_read_status", uniqueConstraints = @UniqueConstraint(columnNames = {"notification_id", "user_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationReadStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private UserNotification notification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ENotificationStatus status; // READ hoặc DELETED

    private Instant updatedAt;
}