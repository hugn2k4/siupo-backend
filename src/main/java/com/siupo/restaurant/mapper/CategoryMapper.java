package com.siupo.restaurant.mapper;

import com.siupo.restaurant.dto.request.CategoryRequest;
import com.siupo.restaurant.dto.response.CategoryResponse;
import com.siupo.restaurant.model.Category;
import com.siupo.restaurant.model.Image;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryMapper {
    private final ImageMapper imageMapper;

    public CategoryResponse toResponse(Category category) {
        if (category == null) return null;
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .image(imageMapper.toDto(category.getImage()))
                .build();
    }

    public Category toEntity(CategoryResponse request) {
        if (request == null) return null;
        return Category.builder()
                .name(request.getName())
                .image(imageMapper.buildEntity(request.getImageName(), request.getImageUrl()))
                .build();
    }

    public CategoryResponse requestToResponse(CategoryRequest request) {
        if (request == null) return null;
        return CategoryResponse.builder()
                .id(request.getId())
                .name(request.getName())
                .imageUrl(request.getImageUrl())
                .imageName(request.getImageName())
                .image(imageMapper.toDto(
                        imageMapper.buildEntity(request.getImageName(), request.getImageUrl())
                ))
                .build();
    }

    public void updateCategoryFromRequest(Category category, CategoryRequest request) {
        if (category == null || request == null) return;
        category.setName(request.getName());
        if (request.getImageUrl() != null) {
            if (category.getImage() == null) {
                category.setImage(
                        Image.builder()
                                .name(request.getImageName())
                                .url(request.getImageUrl())
                                .build()
                );
            } else {
                category.getImage().setName(request.getImageName());
                category.getImage().setUrl(request.getImageUrl());
            }
        }
    }
}
