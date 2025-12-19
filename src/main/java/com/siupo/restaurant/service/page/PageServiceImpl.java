package com.siupo.restaurant.service.page;

import com.siupo.restaurant.dto.response.*;
import com.siupo.restaurant.mapper.ProductMapper;
import com.siupo.restaurant.model.Product;
import com.siupo.restaurant.repository.ProductRepository;
import com.siupo.restaurant.service.category.CategoryService;
import com.siupo.restaurant.service.combo.ComboService;
import com.siupo.restaurant.service.tag.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService {
    private final ComboService comboService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public ShopDataResponse getInitialDataShop() {
        // 1. Lấy tất cả combos available
        List<ComboResponse> availableCombos = comboService.getAvailableCombos();
        // 2. Lấy tất cả categories
        List<CategoryResponse> categories = categoryService.getAllCategories();
        // 3. Lấy tất cả tags
        List<TagResponse> tags = tagService.getAllTags();
        // 4. Lấy 4 sản phẩm mới nhất với rating (cho sidebar)
        List<ProductResponse> latestProducts = getLatestProducts(4);
        // 5. Lấy 15 products đầu tiên cho trang chính (page 0, size 15, sắp xếp theo id)
        List<ProductResponse> initialProducts = getInitialProducts(15);
        return ShopDataResponse.builder()
                .combos(availableCombos)
                .categories(categories)
                .products(initialProducts)
                .tags(tags)
                .latestProducts(latestProducts)
                .build();
    }

    private List<ProductResponse> getLatestProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "id"));
        List<Product> products = productRepository.findAll(pageable).getContent();
        return products.stream()
                .map(productMapper::toResponse)
                .toList();
    }

    private List<ProductResponse> getInitialProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "id"));
        List<Product> products = productRepository.findAll(pageable).getContent();
        return products.stream()
                .map(productMapper::toResponse)
                .toList();
    }
}
