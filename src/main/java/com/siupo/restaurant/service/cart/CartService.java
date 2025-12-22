package com.siupo.restaurant.service.cart;

import com.siupo.restaurant.dto.request.AddToCartRequest;
import com.siupo.restaurant.dto.response.CartResponse;
import com.siupo.restaurant.model.User;

public interface CartService {
    CartResponse getCartByUser(User user);
    CartResponse addItemToCart(User user, AddToCartRequest request);
    CartResponse updateItemQuantity(User user, Long itemId, Long quantity);
    CartResponse removeCartItem(User user, Long itemId);
}
