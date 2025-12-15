package com.siupo.restaurant.service.category;

import com.siupo.restaurant.dto.CategoryDTO;
import com.siupo.restaurant.dto.response.MessageDataReponse;

import java.util.List;

public interface CategoryService {

    List<CategoryDTO> getAllCategories();

    CategoryDTO addCategory(CategoryDTO categoryDTO);

    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO);

    MessageDataReponse deleteCategory(Long id);
}