package com.siupo.restaurant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO {
    private Long id;
    private String content;
    private Double rate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userId;
}