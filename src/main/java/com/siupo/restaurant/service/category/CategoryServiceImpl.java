package com.siupo.restaurant.service.category;

import com.siupo.restaurant.dto.request.CategoryRequest;
import com.siupo.restaurant.dto.response.CategoryResponse;
import com.siupo.restaurant.exception.base.ErrorCode;
import com.siupo.restaurant.exception.business.BadRequestException;
import com.siupo.restaurant.exception.business.ResourceNotFoundException;
import com.siupo.restaurant.mapper.CategoryMapper;
import com.siupo.restaurant.model.Category;
import com.siupo.restaurant.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse addCategory(CategoryRequest categoryRequest) {
        Category category = categoryMapper.toEntity(categoryMapper.requestToResponse(categoryRequest));
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest categoryRequest) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
        categoryMapper.updateCategoryFromRequest(category, categoryRequest);
        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(updatedCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_EMPTY));
        try {
            categoryRepository.delete(category);
            categoryRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException(ErrorCode.CANNOT_DELETE_CATEGORY);
        }
    }
}