package com.siupo.restaurant.mapper;

import com.siupo.restaurant.dto.response.WishlistResponse;
import com.siupo.restaurant.model.Wishlist;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WishListMapper {
    private final WishListItemMapper wishListItemMapper;

    public WishlistResponse toResponse(Wishlist wishlist) {
        return WishlistResponse.builder()
                .id(wishlist.getId())
                .userId(wishlist.getUser().getId())
                .items(wishlist.getItems().stream()
                        .map(wishListItemMapper::toResponse)
                        .collect(Collectors.toList()))
                .totalItems(wishlist.getItems().size())
                .build();
    }
}
