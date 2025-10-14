package com.siupo.restaurant.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Long id;
    private String content;
    private Double rate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userId;
}