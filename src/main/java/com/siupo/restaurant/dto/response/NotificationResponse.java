package com.siupo.restaurant.dto.response;

import com.siupo.restaurant.enums.ENotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long id;
    private Instant sentAt;
    private String title;
    private String content;
    private ENotificationStatus status;
    private Long userId;
}