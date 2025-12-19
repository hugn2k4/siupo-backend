package com.siupo.restaurant.mapper;

import com.siupo.restaurant.dto.CartItemDTO;
import com.siupo.restaurant.dto.ImageDTO;
import com.siupo.restaurant.dto.ProductDTO;
import com.siupo.restaurant.dto.response.CartResponse;
import com.siupo.restaurant.dto.response.ComboResponse;
import com.siupo.restaurant.model.Cart;
import com.siupo.restaurant.model.ProductImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CartMapper {
    private final ComboMapper comboMapper;

    public CartResponse toResponse(Cart cart) {
        if (cart == null) return null;
        // 1. Map từng CartItem thành CartItemDTO
        List<CartItemDTO> itemDTOs = cart.getItems().stream()
                .map(item -> {
                    ProductDTO productDTO = null;
                    ComboResponse comboDTO = null;
                    // Map Product
                    if (item.getProduct() != null) {
                        productDTO = ProductDTO.builder()
                                .id(item.getProduct().getId())
                                .name(item.getProduct().getName())
                                .description(item.getProduct().getDescription())
                                .price(item.getProduct().getPrice())
                                // Map ảnh
                                .images(item.getProduct().getImages().stream()
                                        .map(img -> {
                                            ImageDTO dto = new ImageDTO();
                                            dto.setId(img.getId());
                                            dto.setUrl(img.getUrl());
                                            dto.setName(img.getName());
                                            return dto;
                                        }).toList())
                                .imageUrls(item.getProduct().getImages().stream()
                                        .map(ProductImage::getUrl).toList())
                                .build();
                    }
                    // Map Combo
                    if (item.getCombo() != null) {
                        comboDTO = comboMapper.toResponse(item.getCombo());
                    }
                    return CartItemDTO.builder()
                            .id(item.getId())
                            .product(productDTO)
                            .combo(comboDTO)
                            .price(item.getPrice())
                            .quantity(item.getQuantity())
                            .build();
                })
                .collect(Collectors.toList());
        // 2. Đảo ngược danh sách để hiển thị mục mới nhất ở trên cùng
        Collections.reverse(itemDTOs);
        // 3. Build response cuối cùng
        return CartResponse.builder()
                .id(cart.getId())
                .totalPrice(cart.getTotalPrice())
                .items(itemDTOs)
                .build();
    }
}
