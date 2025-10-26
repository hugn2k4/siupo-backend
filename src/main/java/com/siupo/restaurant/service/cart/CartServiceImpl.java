package com.siupo.restaurant.service.cart;

import com.siupo.restaurant.model.Cart;
import com.siupo.restaurant.model.CartItem;
import com.siupo.restaurant.model.Product;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.repository.CartItemRepository;
import com.siupo.restaurant.repository.CartRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public Cart getCartByUser(User user){
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
    }

    @Override
    public Cart addItemToCart(User user, Product item, int quantity) {
        Cart cart = getCartByUser(user);

        Optional<CartItem> existingCartItem = cart.getItems().stream()
                .filter(cartItem -> cartItem.getProduct().getId().equals(item.getId()))
                .findFirst();
        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setPrice(item.getPrice()*cartItem.getQuantity());
        } else {
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(item)
                    .quantity((long)quantity)
                    .price(item.getPrice())
                    .build();
            cart.getItems().add(cartItem);
        }
        double totalPrice = cart.getItems().stream()
                .mapToDouble(CartItem::getPrice)
                .sum();
        cart.setTotalPrice(totalPrice);

        return cartRepository.save(cart);
    }

    @Override
    public Cart updateItemQuantity(User user, Long itemId, Long quantity) {
        Cart cart = getCartByUser(user);

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);

        for (Iterator<CartItem> iterator = cartItems.iterator(); iterator.hasNext();) {
            CartItem cartItem = iterator.next();

            if (cartItem.getId().equals(itemId)) {
                if (quantity <= 0) {
                    iterator.remove();
                    cartItemRepository.delete(cartItem);
                } else {
                    cartItem.setQuantity(quantity);
                    cartItem.setPrice(cartItem.getProduct().getPrice() * quantity);
                    cartItemRepository.save(cartItem);
                }
                break;
            }
        }

        double totalPrice = cartItems.stream()
                .mapToDouble(CartItem::getPrice)
                .sum();

        cart.setTotalPrice(totalPrice);
        return cartRepository.save(cart);
    }

    @Override
    public Cart removeCartItem(User user, Long itemId) {
        Cart cart = getCartByUser(user);

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);

        for (Iterator<CartItem> iterator = cartItems.iterator(); iterator.hasNext();) {
            CartItem cartItem = iterator.next();

            if (cartItem.getId().equals(itemId)) {
                iterator.remove();
                cartItemRepository.delete(cartItem);
                break;
            }
        }

        double totalPrice = cartItems.stream()
                .mapToDouble(CartItem::getPrice)
                .sum();

        cart.setTotalPrice(totalPrice);
        return cartRepository.save(cart);
    }
}
