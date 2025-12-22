package com.siupo.restaurant.mapper;

import com.siupo.restaurant.dto.response.TagResponse;
import com.siupo.restaurant.model.ProductTag;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {
    public TagResponse toResponse(ProductTag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .createdAt(tag.getCreatedAt())
                .updatedAt(tag.getUpdatedAt())
                .build();
    }
}
