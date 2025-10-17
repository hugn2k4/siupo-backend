package com.siupo.restaurant.dto;

import com.siupo.restaurant.enums.EProductStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Long categoryId;
    private String categoryName;
    private List<String> imageUrls; // Chỉ lấy url từ ProductImage
    private List<ReviewDTO> reviews;
    private EProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}