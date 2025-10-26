package com.siupo.restaurant.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.siupo.restaurant.dto.CartItemDTO;
import com.siupo.restaurant.model.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
    @JsonIgnore
    private Long id;
    private Double totalPrice;
    private List<CartItemDTO> items;

}
