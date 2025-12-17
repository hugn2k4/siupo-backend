package com.siupo.restaurant.service.cart;

import com.siupo.restaurant.dto.request.AddToCartRequest;
import com.siupo.restaurant.dto.response.CartResponse;
import com.siupo.restaurant.exception.base.ErrorCode;
import com.siupo.restaurant.exception.business.BadRequestException;
import com.siupo.restaurant.exception.business.ResourceNotFoundException;
import com.siupo.restaurant.mapper.CartMapper;
import com.siupo.restaurant.model.*;
import com.siupo.restaurant.repository.CartRepository;
import com.siupo.restaurant.service.combo.ComboService;
import com.siupo.restaurant.service.product.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ProductService productService;
    private final ComboService comboService;
    private final CartMapper cartMapper;

    @Override
    @Transactional
    public CartResponse getCartByUser(User user) {
        Cart cart = getOrCreateCartEntity(user);
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItemToCart(User user, AddToCartRequest request) {
        boolean hasProduct = request.getProductId() != null;
        boolean hasCombo = request.getComboId() != null;
        if (!hasProduct && !hasCombo) {
            throw new BadRequestException(ErrorCode.MISSING_SELECTION);
        }
        if (hasProduct && hasCombo) {
            throw new BadRequestException(ErrorCode.CONFLICTING_SELECTION);
        }
        if (request.getQuantity() <= 0) {
            throw new BadRequestException(ErrorCode.INVALID_QUANTITY);
        }
        Cart cart = getOrCreateCartEntity(user);
        if (hasProduct) {
            Product product = productService.getProductEntityById(request.getProductId());
            return addProductToCart(cart, product, request.getQuantity());
        }
        Combo combo = comboService.getComboById(request.getComboId());
        return addComboToCart(cart, combo, request.getQuantity());
    }

    @Override
    @Transactional
    public CartResponse updateItemQuantity(User user, Long itemId, Long quantity) {
        Cart cart = getOrCreateCartEntity(user);
        Optional<CartItem> itemOpt = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst();
        if (itemOpt.isEmpty()) {
            throw new ResourceNotFoundException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        CartItem cartItem = itemOpt.get();
        if (quantity <= 0) {
            cart.getItems().remove(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            double unitPrice = (cartItem.getProduct() != null)
                    ? cartItem.getProduct().getPrice()
                    : cartItem.getCombo().getBasePrice();
            cartItem.setPrice(unitPrice * quantity);
        }
        return updateCartAndReturnResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeCartItem(User user, Long itemId) {
        Cart cart = getOrCreateCartEntity(user);
        CartItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CART_ITEM_NOT_FOUND));
        cart.getItems().remove(itemToRemove);
        return updateCartAndReturnResponse(cart);
    }

    // ==================== Private Helper Methods ====================

    private Cart getOrCreateCartEntity(User user) {
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
    }

    private CartResponse addProductToCart(Cart cart, Product product, int quantity) {
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct() != null)
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setPrice(product.getPrice() * item.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity((long) quantity)
                    .price(product.getPrice() * quantity)
                    .build();
            cart.getItems().add(newItem);
        }
        return updateCartAndReturnResponse(cart);
    }

    private CartResponse addComboToCart(Cart cart, Combo combo, int quantity) {
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getCombo() != null)
                .filter(item -> item.getCombo().getId().equals(combo.getId()))
                .findFirst();
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setPrice(combo.getBasePrice() * item.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .combo(combo)
                    .quantity((long) quantity)
                    .price(combo.getBasePrice() * quantity)
                    .build();
            cart.getItems().add(newItem);
        }
        return updateCartAndReturnResponse(cart);
    }

    private CartResponse updateCartAndReturnResponse(Cart cart) {
        double totalCartPrice = cart.getItems().stream()
                .mapToDouble(CartItem::getPrice)
                .sum();
        cart.setTotalPrice(totalCartPrice);
        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toResponse(savedCart);
    }
}
