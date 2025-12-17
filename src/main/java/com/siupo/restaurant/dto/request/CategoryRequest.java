package com.siupo.restaurant.dto.request;

import com.siupo.restaurant.dto.ImageDTO;
import lombok.Data;

@Data
public class CategoryRequest {
    private Long id;
    private String name;
    private String imageUrl;
    private String imageName;
    private ImageDTO image;
}