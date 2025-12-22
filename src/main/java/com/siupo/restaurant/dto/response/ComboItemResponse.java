package com.siupo.restaurant.dto.response;

import com.siupo.restaurant.model.ComboItem;
import com.siupo.restaurant.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComboItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private Double productPrice;
    private Integer quantity;
    private Integer displayOrder;
    
    public static ComboItemResponse mapToResponse(ComboItem comboItem) {
        if (comboItem == null) return null;
        
        Product product = comboItem.getProduct();
        String imageUrl = product.getImages() != null && !product.getImages().isEmpty()
                ? product.getImages().get(0).getUrl()
                : null;
        
        return ComboItemResponse.builder()
                .id(comboItem.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productImageUrl(imageUrl)
                .productPrice(product.getPrice())
                .quantity(comboItem.getQuantity())
                .displayOrder(comboItem.getDisplayOrder())
                .build();
    }
}
