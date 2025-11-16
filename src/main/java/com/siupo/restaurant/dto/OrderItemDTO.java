package com.siupo.restaurant.dto;

import com.siupo.restaurant.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    private Long id;
    private Long productId;
    private String productName;
    private Long quantity;
    private Double price;
    private Double subTotal;
    private String productImageUrl;
    private String note;
    private Boolean reviewed;

    private String productCategoryName;

    public static OrderItemDTO toDTO(OrderItem item) {
        String productImageUrl = null;
        String categoryName = null;

        if (item.getProduct() != null) {
            if (item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()) {
                productImageUrl = item.getProduct().getImages().get(0).getUrl();
            }
            if (item.getProduct().getCategory() != null) {
                categoryName = item.getProduct().getCategory().getName();
            }
        }

        return OrderItemDTO.builder()
                .id(item.getId())
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProduct() != null ? item.getProduct().getName() : null)
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .note(item.getNote())
                .reviewed(item.getReviewed())
                .subTotal(item.getPrice() != null && item.getQuantity() != null
                        ? item.getPrice() * item.getQuantity() : null)
                .productImageUrl(productImageUrl)
                .productCategoryName(categoryName)
                .build();
    }

}
