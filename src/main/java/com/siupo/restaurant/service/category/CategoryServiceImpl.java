package com.siupo.restaurant.service.category;

import com.siupo.restaurant.dto.CategoryDTO;
import com.siupo.restaurant.dto.ImageDTO;
import com.siupo.restaurant.dto.response.MessageDataReponse;
import com.siupo.restaurant.model.Category;
import com.siupo.restaurant.model.Image;
import com.siupo.restaurant.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;
    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category không tồn tại với ID: " + id));
    }

    private CategoryDTO mapToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());

        if (category.getImage() != null) {
            ImageDTO imageDTO = ImageDTO.builder()
                    .id(category.getImage().getId())
                    .url(category.getImage().getUrl())
                    .name(category.getImage().getName())
                    .build();
            dto.setImage(imageDTO);
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToDTO) // Dùng helper mới
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO addCategory(CategoryDTO categoryDTO) {
        Image image = Image.builder()
                .name(categoryDTO.getImageName())
                .url(categoryDTO.getImageUrl())
                .build();

        Category category = Category.builder()
                .name(categoryDTO.getName())
                .image(image)
                .build();

        Category savedCategory = categoryRepository.save(category);

        return mapToDTO(savedCategory);
    }

    @Override
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = findCategoryById(id);

        category.setName(categoryDTO.getName());

        Image existingImage = category.getImage();
        if (existingImage == null) {
            existingImage = Image.builder().build();
            category.setImage(existingImage);
        }
        existingImage.setName(categoryDTO.getImageName());
        existingImage.setUrl(categoryDTO.getImageUrl());

        Category updatedCategory = categoryRepository.save(category);

        return mapToDTO(updatedCategory);
    }

    @Override
    public MessageDataReponse deleteCategory(Long id) {
        try {
            Category category = findCategoryById(id);
            categoryRepository.delete(category);
            return new MessageDataReponse(true, "200", "Xóa danh mục thành công");

        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return new MessageDataReponse(false, "400", "Không thể xóa danh mục này vì đang được sử dụng trong sản phẩm");

        } catch (RuntimeException e) {
            return new MessageDataReponse(false, "404", "Không tìm thấy danh mục với ID: " + id);

        } catch (Exception e) {
            return new MessageDataReponse(false, "500", "Đã xảy ra lỗi khi xóa danh mục: " + e.getMessage());
        }
    }
}