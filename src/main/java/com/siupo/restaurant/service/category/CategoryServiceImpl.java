package com.siupo.restaurant.service.category;

import com.siupo.restaurant.dto.CategoryDTO;
import com.siupo.restaurant.dto.response.MessageDataReponse;
import com.siupo.restaurant.model.Category;
import com.siupo.restaurant.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> {
                    CategoryDTO dto = new CategoryDTO();
                    dto.setId(category.getId());
                    dto.setName(category.getName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO addCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        category.setName(categoryDTO.getName());
        Category savedCategory = categoryRepository.save(category);
        categoryDTO.setId(savedCategory.getId());
        return categoryDTO;
    }

    @Override
    public CategoryDTO updateCategory(Long id,CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setName(categoryDTO.getName());
        categoryRepository.save(category);
        categoryDTO.setId(id);
        return categoryDTO;
    }

    @Override
    public MessageDataReponse deleteCategory(Long id) {
        try {
            if (!categoryRepository.existsById(id)) {
                return new MessageDataReponse(false, "404", "Không tìm thấy danh mục với ID: " + id);
            }
            categoryRepository.deleteById(id);
            return new MessageDataReponse(true, "200", "Xóa danh mục thành công");

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return new MessageDataReponse(false, "400", "Không thể xóa danh mục này vì đang được sử dụng trong sản phẩm");

        } catch (Exception e) {
            return new MessageDataReponse(false, "500", "Đã xảy ra lỗi khi xóa danh mục: " + e.getMessage());
        }
    }

}