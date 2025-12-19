package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.request.ProductRequest;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.ProductResponse;
import com.siupo.restaurant.dto.response.ReviewResponse;
import com.siupo.restaurant.exception.base.ErrorCode;
import com.siupo.restaurant.exception.business.BadRequestException;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.service.product.ProductService;
import com.siupo.restaurant.service.review.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @AuthenticationPrincipal User user) {
        Page<ProductResponse> productsResponse = productService.getAllProducts(user, page, size, sortBy);
        ApiResponse<Page<ProductResponse>> response = ApiResponse.<Page<ProductResponse>>builder()
                .success(true)
                .code("200")
                .message("Products retrieved successfully")
                .data(productsResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        ProductResponse productResponse = productService.getProductById(user, id);
        ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                .success(true)
                .code("200")
                .message("Product retrieved successfully")
                .data(productResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchAndFilterProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "id,asc") String sortBy,
            @AuthenticationPrincipal User user) {
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new BadRequestException(ErrorCode.SEARCH_PRICE_INVALID);
        }
        if (categoryIds != null && categoryIds.isEmpty()) {
            throw new BadRequestException(ErrorCode.SEARCH_CATEGORY_NOT_BLANK);
        }
        Page<ProductResponse> products = productService.searchAndFilterProducts(user,name, categoryIds, minPrice, maxPrice, page, size, sortBy);
        String message = products.isEmpty()
                ? "No products found matching your criteria"
                : "Products retrieved successfully";
        ApiResponse<Page<ProductResponse>> response = ApiResponse.<Page<ProductResponse>>builder()
                .success(true)
                .code("200")
                .message(message)
                .data(products)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse productResponse = productService.createProduct(request);
        ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                .success(true)
                .code("201")
                .data(productResponse)
                .message("Product created successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse productResponse = productService.updateProduct(id, request);
        ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                .success(true)
                .code("202")
                .data(productResponse)
                .message("Product updated successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProductById(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .code("203")
                .message("Product deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProductStatus(
            @PathVariable Long id) {
        ProductResponse product = productService.updateProductStatus(id);
        ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                .success(true)
                .code("202")
                .data(product)
                .message("Product status updated successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getProductReviews(
            @PathVariable Long id) {
        List<ReviewResponse> reviews = reviewService.getReviewsByProductId(id);
        ApiResponse<List<ReviewResponse>> response = ApiResponse.<List<ReviewResponse>>builder()
                .success(true)
                .code("200")
                .message("Reviews retrieved successfully")
                .data(reviews)
                .build();
        return ResponseEntity.ok(response);
    }
}