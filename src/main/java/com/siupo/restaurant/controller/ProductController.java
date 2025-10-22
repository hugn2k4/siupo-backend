package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.ProductDTO;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.enums.EProductStatus;
import com.siupo.restaurant.service.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        Page<ProductDTO> products = productService.getAllProducts(page, size, sortBy);
        ApiResponse<Page<ProductDTO>> response = ApiResponse.<Page<ProductDTO>>builder()
                .success(true)
                .code("200")
                .message("Products retrieved successfully")
                .data(products)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        ApiResponse<ProductDTO> response = ApiResponse.<ProductDTO>builder()
                .success(true)
                .code("200")
                .message("Product retrieved successfully")
                .data(product)
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
}