package com.siupo.restaurant.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private Double price;
    private Long quantity;
}
