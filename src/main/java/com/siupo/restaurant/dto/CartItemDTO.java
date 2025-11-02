package com.siupo.restaurant.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemDTO {
    private Long id;
    private ProductDTO product;
    private Double price;
    private Long quantity;
}
