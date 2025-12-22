package com.siupo.restaurant.service.category;

import com.siupo.restaurant.dto.request.CategoryRequest;
import com.siupo.restaurant.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllCategories();
    CategoryResponse addCategory(CategoryRequest categoryResponse);
    CategoryResponse updateCategory(Long id, CategoryRequest categoryResponse);
    void deleteCategory(Long id);
}