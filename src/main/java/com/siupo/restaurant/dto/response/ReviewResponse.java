package com.siupo.restaurant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    private Long id;
    private Long orderItemId;
    private Long productId;
    private String productName;
    private Double rating;
    private String content;
    private List<String> imageUrls;
    private String userName;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
