package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.request.AddToCartRequest;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.CartResponse;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(@AuthenticationPrincipal User user, @RequestBody AddToCartRequest request) {
        CartResponse cartResponse = cartService.addItemToCart(user, request);
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
        CartResponse cartResponse = cartService.updateItemQuantity(user, itemId, quantity);
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
        CartResponse cartResponse = cartService.removeCartItem(user, itemId);
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
        CartResponse cartResponse = cartService.getCartByUser(user);
        ApiResponse<CartResponse> response = ApiResponse.<CartResponse>builder()
                .success(true)
                .code("200")
                .message("Cart retrieved successfully")
                .data(cartResponse)
                .build();
        return ResponseEntity.ok(response);
    }
}
