package com.siupo.restaurant.service.cart;

import com.siupo.restaurant.dto.request.AddToCartRequest;
import com.siupo.restaurant.exception.BadRequestException;
import com.siupo.restaurant.model.*;
import com.siupo.restaurant.repository.CartItemRepository;
import com.siupo.restaurant.repository.CartRepository;
import com.siupo.restaurant.service.combo.ComboService;
import com.siupo.restaurant.service.product.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final ComboService comboService;

    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductService productService, ComboService comboService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
        this.comboService = comboService;
    }

    @Override
    public Cart getCartByUser(User user) {
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });

        Collections.reverse(cart.getItems());
        return cart;
    }

    @Override
    public Cart addItemToCart(User user, AddToCartRequest request) {
        if (request.getProductId() == null && request.getComboId() == null) {
            throw new BadRequestException("Either productId or comboId is required");
        }

        if (request.getProductId() != null && request.getComboId() != null) {
            throw new BadRequestException("Only one of productId or comboId should be provided");
        }
        Cart cart = getCartByUser(user);

        if (request.getProductId() != null) {
            Product product = productService.getProductEntityById(request.getProductId());
            return addProductToCart(cart, product, request.getQuantity());
        }

        if (request.getComboId() != null) {
            Combo combo = comboService.getComboById(request.getComboId());
            return addComboToCart(cart, combo, request.getQuantity());
        }
        throw new BadRequestException("Either productId or comboId must be provided.");
    }

    private Cart addProductToCart(Cart cart, Product item, int quantity) {
        Optional<CartItem> existingCartItem = cart.getItems().stream()
                .filter(ci -> ci.getProduct() != null)
                .filter(ci -> ci.getProduct().getId().equals(item.getId()))
                .findFirst();
        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setPrice(item.getPrice() * cartItem.getQuantity());
        } else {
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(item)
                    .quantity((long) quantity)
                    .price(item.getPrice() * quantity)
                    .build();

            cart.getItems().add(cartItem);
        }

        cart.setTotalPrice(
                cart.getItems().stream()
                        .mapToDouble(CartItem::getPrice)
                        .sum()
        );

        return cartRepository.save(cart);
    }


    private Cart addComboToCart(Cart cart, Combo combo, int quantity) {

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getCombo() != null)
                .filter(i -> i.getCombo().getId().equals(combo.getId()))
                .findFirst();

        if (existing.isPresent()) {
            CartItem cartItem = existing.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setPrice(combo.getBasePrice() * cartItem.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .combo(combo)
                    .quantity((long) quantity)
                    .price(combo.getBasePrice() * quantity)
                    .build();

            cart.getItems().add(newItem);
        }

        cart.setTotalPrice(
                cart.getItems().stream()
                        .mapToDouble(CartItem::getPrice)
                        .sum()
        );

        return cartRepository.save(cart);
    }

    @Override
    public Cart updateItemQuantity(User user, Long itemId, Long quantity) {
        Cart cart = getCartByUser(user);

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);

        for (Iterator<CartItem> iterator = cartItems.iterator(); iterator.hasNext(); ) {
            CartItem cartItem = iterator.next();

            if (cartItem.getId().equals(itemId)) {
                if (quantity <= 0) {
                    iterator.remove();
                    cartItemRepository.delete(cartItem);
                } else {
                    cartItem.setQuantity(quantity);
                    // FIX: kiểm tra loại item
                    if (cartItem.getProduct() != null) {
                        cartItem.setPrice(cartItem.getProduct().getPrice() * quantity);
                    } else if (cartItem.getCombo() != null) {
                        cartItem.setPrice(cartItem.getCombo().getBasePrice() * quantity);
                    }
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
