package com.siupo.restaurant.service.wishlist;

import com.siupo.restaurant.dto.response.WishlistResponse;
import com.siupo.restaurant.model.User;

public interface WishlistService {
    WishlistResponse getWishlist(User user);
    WishlistResponse addToWishlist(User user, Long productId);
    WishlistResponse removeFromWishlist(User user, Long productId);
    void clearWishlist(User user);
    boolean isProductInWishlist(User user, Long productId);
}