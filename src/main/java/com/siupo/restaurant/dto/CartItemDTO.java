package com.siupo.restaurant.dto;

import com.siupo.restaurant.dto.response.ComboResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemDTO {
    private Long id;
    private ProductDTO product;
    private ComboResponse combo;
    private Double price;
    private Long quantity;
}
