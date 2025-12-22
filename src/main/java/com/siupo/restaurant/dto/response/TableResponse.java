package com.siupo.restaurant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableResponse {
    private Long id;
    private String tableNumber;
    private Integer seat;
    private String qr;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
