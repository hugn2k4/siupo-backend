    package com.siupo.restaurant.controller;

    import com.siupo.restaurant.dto.request.WishlistRequest;
    import com.siupo.restaurant.dto.response.WishlistResponse;
    import com.siupo.restaurant.model.User;
    import com.siupo.restaurant.service.wishlist.WishlistService;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.access.prepost.PreAuthorize;
    import org.springframework.security.core.Authentication;
    import org.springframework.web.bind.annotation.*;

    import java.util.HashMap;
    import java.util.Map;

    @RestController
    @RequestMapping("/api/wishlist")
    @RequiredArgsConstructor
    @PreAuthorize("hasRole('CUSTOMER')")
    public class WishlistController {

        private final WishlistService wishlistService;

        @GetMapping
        public ResponseEntity<WishlistResponse> getWishlist(Authentication authentication) {
            Long userId = getUserIdFromAuthentication(authentication);
            WishlistResponse wishlist = wishlistService.getWishlist(userId);
            return ResponseEntity.ok(wishlist);
        }

        @PostMapping("/items")
        public ResponseEntity<WishlistResponse> addToWishlist(
                Authentication authentication,
                @Valid @RequestBody WishlistRequest request) {
            Long userId = getUserIdFromAuthentication(authentication);
            WishlistResponse wishlist = wishlistService.addToWishlist(userId, request.getProductId());
            return ResponseEntity.status(HttpStatus.CREATED).body(wishlist);
        }

        @DeleteMapping("/items/{productId}")
        public ResponseEntity<WishlistResponse> removeFromWishlist(
                Authentication authentication,
                @PathVariable Long productId) {
            Long userId = getUserIdFromAuthentication(authentication);
            WishlistResponse wishlist = wishlistService.removeFromWishlist(userId, productId);
            return ResponseEntity.ok(wishlist);
        }

        @DeleteMapping("/items")
        public ResponseEntity<Map<String, String>> clearWishlist(Authentication authentication) {
            Long userId = getUserIdFromAuthentication(authentication);
            wishlistService.clearWishlist(userId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Wishlist cleared successfully");
            return ResponseEntity.ok(response);
        }

        @GetMapping("/check/{productId}")
        public ResponseEntity<Map<String, Boolean>> checkProductInWishlist(
                Authentication authentication,
                @PathVariable Long productId) {
            Long userId = getUserIdFromAuthentication(authentication);
            boolean isInWishlist = wishlistService.isProductInWishlist(userId, productId);

            Map<String, Boolean> response = new HashMap<>();
            response.put("isInWishlist", isInWishlist);
            return ResponseEntity.ok(response);
        }

        private Long getUserIdFromAuthentication(Authentication authentication) {
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                return user.getId();
            }
            throw new IllegalStateException("Unable to extract user ID from authentication");
        }
    }