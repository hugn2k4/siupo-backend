package com.siupo.restaurant.service.wishlist;

import com.siupo.restaurant.dto.response.WishlistResponse;

public interface WishlistService {
    WishlistResponse getWishlist(Long userId);
    WishlistResponse addToWishlist(Long userId, Long productId);
    WishlistResponse removeFromWishlist(Long userId, Long productId);
    void clearWishlist(Long userId);
    boolean isProductInWishlist(Long userId, Long productId);
}