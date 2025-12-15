package com.siupo.restaurant.dto;

import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private String imageUrl;
    private String imageName;
    private ImageDTO image;
}