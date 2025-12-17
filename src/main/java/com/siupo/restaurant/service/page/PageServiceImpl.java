package com.siupo.restaurant.service.page;

import com.siupo.restaurant.dto.response.*;
import com.siupo.restaurant.model.Product;
import com.siupo.restaurant.model.ProductImage;
import com.siupo.restaurant.model.ProductTag;
import com.siupo.restaurant.model.Review;
import com.siupo.restaurant.repository.ProductRepository;
import com.siupo.restaurant.repository.ReviewRepository;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService {
    private final ComboService comboService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

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
        List<ProductWithRatingResponse> latestProducts = getLatestProductsWithRating(4);
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

    private List<ProductWithRatingResponse> getLatestProductsWithRating(int limit) {
        // Lấy sản phẩm mới nhất, sắp xếp theo ID giảm dần
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "id"));
        List<Product> products = productRepository.findAll(pageable).getContent();
        // Lấy productIds để query reviews một lần
        List<Long> productIds = products.stream()
                .map(Product::getId)
                .collect(Collectors.toList());
        // Lấy tất cả reviews cho các products này
        List<Review> reviews = reviewRepository.findByProductIdIn(productIds);
        // Group reviews theo productId và tính rating
        Map<Long, List<Review>> reviewsByProduct = reviews.stream()
                .collect(Collectors.groupingBy(review -> review.getProduct().getId()));
        // Map sang ProductWithRatingResponse
        return products.stream()
                .map(product -> {
                    List<Review> productReviews = reviewsByProduct.getOrDefault(product.getId(), List.of());
                    double averageRating = 0.0;
                    if (!productReviews.isEmpty()) {
                        averageRating = productReviews.stream()
                                .mapToDouble(Review::getRate)
                                .average()
                                .orElse(0.0);
                    }
                    return ProductWithRatingResponse.builder()
                            .id(product.getId())
                            .name(product.getName())
                            .description(product.getDescription())
                            .price(product.getPrice())
                            .imageUrls(product.getImages().stream()
                                    .map(ProductImage::getUrl)
                                    .collect(Collectors.toList()))
                            .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                            .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                            .tags(product.getTags().stream()
                                    .map(ProductTag::getName)
                                    .collect(Collectors.toList()))
                            .averageRating(Math.round(averageRating * 10.0) / 10.0)
                            .reviewCount(productReviews.size())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<ProductResponse> getInitialProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "id"));
        List<Product> products = productRepository.findAll(pageable).getContent();
        return products.stream()
                .map(product -> ProductResponse.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .description(product.getDescription())
                        .price(product.getPrice())
                        .imageUrls(product.getImages().stream()
                                .map(ProductImage::getUrl)
                                .collect(Collectors.toList()))
                        .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                        .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                        .tags(product.getTags().stream()
                                .map(ProductTag::getName)
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }
}
