package com.siupo.restaurant.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class ProductRequest {
    private String name;
    private String description;
    private Double price;
    private Long categoryId;
    private List<String> imageUrls;
}