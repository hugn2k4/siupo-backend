package com.siupo.restaurant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSimpleResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
    private List<String> imageUrls;
}