package com.siupo.restaurant.dto;

import com.siupo.restaurant.enums.EProductStatus;
import com.siupo.restaurant.model.Product;
import com.siupo.restaurant.model.ProductImage;
import com.siupo.restaurant.model.ProductTag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Long categoryId;
    private String categoryName;
    private List<ImageDTO> images;
    private List<String> imageUrls;
    private List<ReviewDTO> reviews;
    private Double rating;
    private Integer reviewCount;
    private List<String> tags;
    private EProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isWishlist;
}