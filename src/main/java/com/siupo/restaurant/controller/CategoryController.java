package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.request.CategoryRequest;
import com.siupo.restaurant.dto.response.CategoryResponse;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.service.category.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> categoriesResponse = categoryService.getAllCategories();
        ApiResponse<List<CategoryResponse>> response = ApiResponse.<List<CategoryResponse>>builder()
                .success(true)
                .code("200")
                .message("Categories retrieved successfully")
                .data(categoriesResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> addCategory(
            @RequestBody CategoryRequest categoryRequest) {
        CategoryResponse categoryResponse = categoryService.addCategory(categoryRequest);
        ApiResponse<CategoryResponse> response = ApiResponse.<CategoryResponse>builder()
                .success(true)
                .code("201")
                .message("Category added successfully")
                .data(categoryResponse)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryRequest categoryRequest) {
        CategoryResponse categoryResponse = categoryService.updateCategory(id, categoryRequest);
        ApiResponse<CategoryResponse> response = ApiResponse.<CategoryResponse>builder()
                .success(true)
                .code("200")
                .message("Category updated successfully")
                .data(categoryResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .code("204")
                .message("Category deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}