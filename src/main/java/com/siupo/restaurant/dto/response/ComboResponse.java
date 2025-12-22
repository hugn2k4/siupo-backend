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
    private Double originalPrice;
    private List<String> imageUrls;
    private List<ComboItemResponse> items;
    private EProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

