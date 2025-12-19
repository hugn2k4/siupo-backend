package com.siupo.restaurant.dto.response;

import com.siupo.restaurant.dto.ReviewDTO;
import com.siupo.restaurant.enums.EProductStatus;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private List<String> imageUrls;
    private Long categoryId;
    private String categoryName;
    private List<ReviewDTO> reviews;
    private Double rating;
    private Integer reviewCount;
    private List<String> tags;
    private EProductStatus status;
    private boolean isWishlist;
}