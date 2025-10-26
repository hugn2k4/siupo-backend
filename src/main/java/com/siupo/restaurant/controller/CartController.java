package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.CartItemDTO;
import com.siupo.restaurant.dto.request.AddToCartRequest;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.CartResponse;
import com.siupo.restaurant.model.Cart;
import com.siupo.restaurant.model.Product;
import com.siupo.restaurant.model.ProductImage;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.service.cart.CartService;
import com.siupo.restaurant.service.product.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")

public class CartController {

    public final CartService cartService;
    private final ProductService productService;

    public CartController(CartService cartService, ProductService productService) {
        this.cartService = cartService;
        this.productService = productService;
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(@AuthenticationPrincipal User user, @RequestBody AddToCartRequest request) {
        Product product = productService.getProductEntityById(request.getProductId());
        Cart cart = cartService.addItemToCart(user, product, request.getQuantity());

        CartResponse cartResponse = mapToCartResponse(cart);

        ApiResponse<CartResponse> response = ApiResponse.<CartResponse>builder()
                .success(true)
                .code("200")
                .message("Item added to cart successfully")
                .data(cartResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(@AuthenticationPrincipal User user, @PathVariable Long itemId,  @RequestParam Long quantity) {
        Cart cart = cartService.updateItemQuantity(user, itemId, quantity);

        CartResponse cartResponse = mapToCartResponse(cart);

        ApiResponse<CartResponse> response = ApiResponse.<CartResponse>builder()
                .success(true)
                .code("200")
                .message("Cart item updated successfully")
                .data(cartResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeCartItem(@AuthenticationPrincipal User user, @PathVariable Long itemId) {
        Cart cart = cartService.removeCartItem(user, itemId);
        CartResponse cartResponse = mapToCartResponse(cart);
        ApiResponse<CartResponse> response = ApiResponse.<CartResponse>builder()
                .success(true)
                .code("200")
                .message("Cart item removed successfully")
                .data(cartResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@AuthenticationPrincipal User user){
        Cart cart = cartService.getCartByUser(user);

        CartResponse cartResponse = mapToCartResponse(cart);

        ApiResponse<CartResponse> response = ApiResponse.<CartResponse>builder()
                .success(true)
                .code("200")
                .message("Cart retrieved successfully")
                .data(cartResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    private CartResponse mapToCartResponse(Cart cart) {
        return CartResponse.builder()
                .id(cart.getId())
                .totalPrice(cart.getTotalPrice())
                .items(cart.getItems().stream()
                        .map(item -> CartItemDTO.builder()
                                .id(item.getId())
                                .productId(item.getProduct().getId())
                                .productName(item.getProduct().getName())
                                .price(item.getPrice())
                                .productImage(item.getProduct().getImages().stream()
                                    .findFirst()
                                    .map(ProductImage::getUrl)
                                    .orElse(null))
                                .quantity(item.getQuantity())
                                .build())
                        .toList())
                .build();
    }

}
