    package com.siupo.restaurant.controller;

    import com.siupo.restaurant.dto.request.WishlistRequest;
    import com.siupo.restaurant.dto.response.ApiResponse;
    import com.siupo.restaurant.dto.response.WishlistResponse;
    import com.siupo.restaurant.model.User;
    import com.siupo.restaurant.service.wishlist.WishlistService;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.access.prepost.PreAuthorize;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
    import org.springframework.web.bind.annotation.*;

    import java.util.HashMap;
    import java.util.Map;

    @RestController
    @RequiredArgsConstructor
    @PreAuthorize("hasRole('CUSTOMER')")
    @RequestMapping("/api/wishlist")
    public class WishlistController {
        private final WishlistService wishlistService;

        @GetMapping
        public ResponseEntity<ApiResponse<WishlistResponse>> getWishlist(@AuthenticationPrincipal User user) {
            WishlistResponse wishlistResponse = wishlistService.getWishlist(user);
            return ResponseEntity.ok(ApiResponse.<WishlistResponse>builder()
                    .code("200")
                    .success(true)
                    .message("Wishlist retrieved successfully")
                    .data(wishlistResponse)
                    .build());
        }

        @PostMapping("/items")
        public ResponseEntity<ApiResponse<WishlistResponse>> addToWishlist(
                @AuthenticationPrincipal User user,
                @Valid @RequestBody WishlistRequest request) {
            WishlistResponse wishlist = wishlistService.addToWishlist(user, request.getProductId());
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<WishlistResponse>builder()
                    .code("201")
                    .success(true)
                    .message("Product added to wishlist successfully")
                    .data(wishlist)
                    .build());
        }

        @DeleteMapping("/items/{productId}")
        public ResponseEntity<ApiResponse<WishlistResponse>> removeFromWishlist(
                @AuthenticationPrincipal User user,
                @PathVariable Long productId) {
            WishlistResponse wishlist = wishlistService.removeFromWishlist(user, productId);
            return ResponseEntity.ok(ApiResponse.<WishlistResponse>builder()
                    .code("200")
                    .success(true)
                    .message("Product removed from wishlist successfully")
                    .data(wishlist)
                    .build());
        }

        @DeleteMapping("/items")
        public ResponseEntity<ApiResponse<Void>> clearWishlist(@AuthenticationPrincipal User user) {
            wishlistService.clearWishlist(user);
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .code("200")
                    .success(true)
                    .message("Wishlist cleared successfully")
                    .data(null)
                    .build());
        }

        @GetMapping("/check/{productId}")
        public ResponseEntity<Map<String, Boolean>> checkProductInWishlist(
                @AuthenticationPrincipal User user,
                @PathVariable Long productId) {
            boolean isInWishlist = wishlistService.isProductInWishlist(user, productId);
            Map<String, Boolean> response = new HashMap<>();
            response.put("isInWishlist", isInWishlist);
            return ResponseEntity.ok(response);
        }
    }