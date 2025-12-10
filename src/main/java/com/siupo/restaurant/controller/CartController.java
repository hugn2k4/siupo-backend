package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.CartItemDTO;
import com.siupo.restaurant.dto.ImageDTO;
import com.siupo.restaurant.dto.ProductDTO;
import com.siupo.restaurant.dto.request.AddToCartRequest;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.CartResponse;
import com.siupo.restaurant.dto.response.ComboResponse;
import com.siupo.restaurant.mapper.ComboMapper;
import com.siupo.restaurant.model.Cart;
import com.siupo.restaurant.model.ProductImage;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.service.cart.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")

public class CartController {

    public final CartService cartService;
    private final ComboMapper comboMapper;

    public CartController(CartService cartService, ComboMapper comboMapper) {
        this.cartService = cartService;
        this.comboMapper = comboMapper;
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(@AuthenticationPrincipal User user, @RequestBody AddToCartRequest request) {
        Cart cart = cartService.addItemToCart(user, request);

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
                        .map(item -> {
                        ProductDTO productDTO = null;
                        ComboResponse comboDTO = null;

                        if (item.getProduct() != null) {
                            productDTO = ProductDTO.builder()
                                    .id(item.getProduct().getId())
                                    .name(item.getProduct().getName())
                                    .description(item.getProduct().getDescription())
                                    .price(item.getProduct().getPrice())
                                    .images(item.getProduct().getImages().stream()
                                            .map(img -> {
                                                ImageDTO dto = new ImageDTO();
                                                dto.setId(img.getId());
                                                dto.setUrl(img.getUrl());
                                                dto.setName(img.getName());
                                                return dto;
                                            })
                                            .toList())
                                    .imageUrls(item.getProduct().getImages().stream()
                                            .map(ProductImage::getUrl)
                                            .toList())
                                    .build();
                        }

                        // Nếu là combo
                        if (item.getCombo() != null) {
                            comboDTO = comboMapper.toResponse(item.getCombo());
                        }

                        return CartItemDTO.builder()
                                .id(item.getId())
                                .product(productDTO)
                                .combo(comboDTO)
                                .price(item.getPrice())
                                .quantity(item.getQuantity())
                                .build();
                    })
                    .toList())
                .build();

//                            ProductDTO productDTO = ProductDTO.builder()
//                                    .id(item.getProduct().getId())
//                                    .name(item.getProduct().getName())
//                                    .description(item.getProduct().getDescription())
//                                    .price(item.getProduct().getPrice())
//                                    .images(item.getProduct().getImages().stream()
//                                            .map(img -> {
//                                                ImageDTO dto = new ImageDTO();
//                                                dto.setId(img.getId());
//                                                dto.setUrl(img.getUrl());
//                                                dto.setName(img.getName());
//                                                return dto;
//                                            })
//                                            .toList())
//                                    .imageUrls(item.getProduct().getImages().stream()
//                                            .map(ProductImage::getUrl)
//                                            .toList())
//                                    .build();
//
//                            return CartItemDTO.builder()
//                                    .id(item.getId())
//                                    .product(productDTO)
//                                    .price(item.getPrice())
//                                    .quantity(item.getQuantity())
//                                    .build();
//                        })
//                        .toList())
//                .build();
    }

}
