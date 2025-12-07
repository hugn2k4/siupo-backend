package com.siupo.restaurant.dto.response;

import com.siupo.restaurant.enums.EProductStatus;
import com.siupo.restaurant.model.Combo;
import com.siupo.restaurant.model.ComboImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComboResponse {
    private Long id;
    private String name;
    private String description;
    private Double basePrice;
    private List<String> imageUrls;
    private List<ComboItemResponse> items;
    private EProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static ComboResponse mapToResponse(Combo combo) {
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
        
        return ComboResponse.builder()
                .id(combo.getId())
                .name(combo.getName())
                .description(combo.getDescription())
                .basePrice(combo.getBasePrice())
                .imageUrls(imageUrls)
                .items(itemResponses)
                .status(combo.getStatus())
                .createdAt(combo.getCreatedAt())
                .updatedAt(combo.getUpdatedAt())
                .build();
    }
}

