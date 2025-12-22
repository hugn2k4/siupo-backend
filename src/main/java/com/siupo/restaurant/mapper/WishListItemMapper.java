package com.siupo.restaurant.mapper;

import com.siupo.restaurant.dto.response.WishlistItemResponse;
import com.siupo.restaurant.model.Product;
import com.siupo.restaurant.model.ProductImage;
import com.siupo.restaurant.model.WishlistItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WishListItemMapper {
    public WishlistItemResponse toResponse(WishlistItem item) {
        Product product = item.getProduct();
        List<String> imageUrls = product.getImages() != null
                ? product.getImages().stream().map(ProductImage::getUrl).toList()
                : null;
        return WishlistItemResponse.builder()
                .id(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productDescription(product.getDescription())
                .productPrice(product.getPrice())
                .productImages(imageUrls)
                .build();
    }
}
