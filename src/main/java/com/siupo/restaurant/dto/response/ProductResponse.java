package com.siupo.restaurant.dto.response;

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
    private List<String> tags;
}