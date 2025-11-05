package com.siupo.restaurant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    private Long id;
    private Long quantity;
    private Double price;
    private String note;
    private Boolean reviewed;
    private ProductSimpleResponse product;
    private LocalDateTime createdAt;
}