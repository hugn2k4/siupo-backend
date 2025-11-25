package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.ProductDTO;
import com.siupo.restaurant.dto.request.ProductRequest;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.ProductResponse;
import com.siupo.restaurant.dto.response.ReviewResponse;
import com.siupo.restaurant.enums.EProductStatus;
import com.siupo.restaurant.model.Product;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.service.product.ProductService;
import com.siupo.restaurant.service.review.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @AuthenticationPrincipal User user) {
        System.out.println("User in getAllProducts: " + user);

        Page<ProductDTO> productsPage = productService.getAllProductsWithWishlist(user, page, size, sortBy);

        ApiResponse<Page<ProductDTO>> response = ApiResponse.<Page<ProductDTO>>builder()
                .success(true)
                .code("200")
                .message("Products retrieved successfully")
                .data(productsPage)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        Long userId = user != null ? user.getId() : null;
        Product product = productService.getProductEntityById(id);
        ProductDTO productDTO = productService.toDTOWithWishlist(product, userId);

        ApiResponse<ProductDTO> response = ApiResponse.<ProductDTO>builder()
                .success(true)
                .code("200")
                .message("Product retrieved successfully")
                .data(productDTO)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> searchAndFilterProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "id,asc") String sortBy) {
        // Kiểm tra dữ liệu đầu vào
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new IllegalArgumentException("minPrice phải nhỏ hơn hoặc bằng maxPrice");
        }
        if (categoryIds != null && categoryIds.isEmpty()) {
            throw new IllegalArgumentException("Danh sách categoryIds không được rỗng");
        }

        Page<ProductDTO> products = productService.searchAndFilterProducts(name, categoryIds, minPrice, maxPrice, page, size, sortBy);
        String message = products.isEmpty() ? "Không tìm thấy sản phẩm nào phù hợp" : "Lọc sản phẩm thành công";
        ApiResponse<Page<ProductDTO>> response = ApiResponse.<Page<ProductDTO>>builder()
                .success(true)
                .code("200")
                .message(message)
                .data(products)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> create(@RequestBody ProductRequest request) {
        ProductResponse productResponse = productService.createProduct(request);
        ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                .success(true)
                .code("201")
                .data(productResponse)
                .message("Product created successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    // Sửa sản phẩm
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> update(@PathVariable Long id, @RequestBody ProductRequest request) {
        ProductResponse productResponse = productService.updateProduct(id, request);
        ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                .success(true)
                .code("200")
                .data(productResponse)
                .message("Product updated successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id) {
        productService.deleteProductById(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .code("200")
                .message("Product deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProductStatus(
            @PathVariable Long id) {
        ProductDTO product = productService.updateProductStatus(id);
        ApiResponse<ProductDTO> response = ApiResponse.<ProductDTO>builder()
                .success(true)
                .code("200")
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