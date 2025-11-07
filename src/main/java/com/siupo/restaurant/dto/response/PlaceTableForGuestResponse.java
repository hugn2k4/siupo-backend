package com.siupo.restaurant.dto.response;

import com.siupo.restaurant.enums.EPlaceTableStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceTableForGuestResponse {
    private Long id;
    private String fullname;
    private String phoneNumber;
    private String email;
    private Integer memberInt;
    private EPlaceTableStatus status;
    private LocalDateTime startedAt;
    private String note;
    private LocalDateTime createdAt;
    private String message;
}