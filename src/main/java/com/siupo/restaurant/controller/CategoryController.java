package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.CategoryDTO;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.MessageDataReponse;
import com.siupo.restaurant.service.category.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryDTO>> addCategory(@RequestBody CategoryDTO categoryDTO) {
        CategoryDTO createdCategory = categoryService.addCategory(categoryDTO);
        ApiResponse<CategoryDTO> response = ApiResponse.<CategoryDTO>builder()
                .success(true)
                .code("201")
                .message("Category added successfully")
                .data(createdCategory)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategory(@PathVariable Long id, @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO updatedCategory = categoryService.updateCategory(id,categoryDTO);
        ApiResponse<CategoryDTO> response = ApiResponse.<CategoryDTO>builder()
                .success(true)
                .code("202")
                .message("Category updated successfully")
                .data(updatedCategory)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        MessageDataReponse messageDataReponse = categoryService.deleteCategory(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(messageDataReponse.isSuccess())
                .code(messageDataReponse.getCode())
                .message(messageDataReponse.getMessage())
                .build();
        return ResponseEntity.ok(response);
    }
}