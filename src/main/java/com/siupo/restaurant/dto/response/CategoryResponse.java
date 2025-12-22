package com.siupo.restaurant.dto.response;

import com.siupo.restaurant.dto.ImageDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String imageUrl;
    private String imageName;
    private ImageDTO image;
}