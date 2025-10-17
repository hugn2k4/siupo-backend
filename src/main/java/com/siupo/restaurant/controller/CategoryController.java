package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.CategoryDTO;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.service.category.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        ApiResponse<List<CategoryDTO>> response = ApiResponse.<List<CategoryDTO>>builder()
                .success(true)
                .code("200")
                .message("Categories retrieved successfully")
                .data(categories)
                .build();
        return ResponseEntity.ok(response);
    }
}