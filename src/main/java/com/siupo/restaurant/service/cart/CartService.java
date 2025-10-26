package com.siupo.restaurant.service.cart;

import com.siupo.restaurant.model.Cart;
import com.siupo.restaurant.model.Product;
import com.siupo.restaurant.model.User;

public interface CartService {
    Cart getCartByUser(User user);
    Cart addItemToCart(User user, Product item, int quantity);
    Cart updateItemQuantity(User user, Long itemId, Long quantity);
    Cart removeCartItem(User user, Long itemId);
//    void clearCart(User user);
}
