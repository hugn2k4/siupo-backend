package com.siupo.restaurant.mapper;

import com.siupo.restaurant.dto.request.CreateComboRequest;
import com.siupo.restaurant.dto.response.ComboItemResponse;
import com.siupo.restaurant.dto.response.ComboResponse;
import com.siupo.restaurant.enums.EProductStatus;
import com.siupo.restaurant.model.Combo;
import com.siupo.restaurant.model.ComboImage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ComboMapper {

    public ComboResponse toResponse(Combo combo) {
        if (combo == null) return null;

        List<String> imageUrls = combo.getImages() != null
                ? combo.getImages().stream()
                .map(ComboImage::getUrl)
                .collect(Collectors.toList())
                : new ArrayList<>();

        List<ComboItemResponse> itemResponses = combo.getItems() != null
                ? combo.getItems().stream()
                .map(ComboItemResponse::mapToResponse)
                .collect(Collectors.toList())
                : new ArrayList<>();

        double originalPrice = combo.getItems() != null
                ? combo.getItems().stream()
                .mapToDouble(item ->
                        item.getProduct().getPrice() * item.getQuantity()
                )
                .sum()
                : 0.0;

        return ComboResponse.builder()
                .id(combo.getId())
                .name(combo.getName())
                .description(combo.getDescription())
                .basePrice(combo.getBasePrice())
                .originalPrice(originalPrice)
                .imageUrls(imageUrls)
                .items(itemResponses)
                .status(combo.getStatus())
                .createdAt(combo.getCreatedAt())
                .updatedAt(combo.getUpdatedAt())
                .build();
    }

    public Combo toEntity(CreateComboRequest request) {
        if (request == null) return null;
        return Combo.builder()
                .name(request.getName())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .status(EProductStatus.AVAILABLE)
                .build();
    }
}
