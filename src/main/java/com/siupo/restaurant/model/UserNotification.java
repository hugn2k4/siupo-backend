package com.siupo.restaurant.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

import com.siupo.restaurant.enums.ENotificationStatus;

@Entity
@Table(name = "user_notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant sentAt;
    private String title;
    @Column(length = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    private ENotificationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isGlobal = false; // TRUE = notification chung cho tất cả
}
