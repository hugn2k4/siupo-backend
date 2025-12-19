package com.siupo.restaurant.service.product;

import com.siupo.restaurant.dto.request.ProductRequest;
import com.siupo.restaurant.dto.response.ProductResponse;
import com.siupo.restaurant.enums.EProductStatus;
import com.siupo.restaurant.exception.base.ErrorCode;
import com.siupo.restaurant.exception.business.BadRequestException;
import com.siupo.restaurant.mapper.ProductMapper;
import com.siupo.restaurant.model.*;
import com.siupo.restaurant.repository.CategoryRepository;
import com.siupo.restaurant.repository.ProductTagRepository;
import com.siupo.restaurant.repository.ProductRepository;
import com.siupo.restaurant.util.PageableUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductTagRepository productTagRepository;
    private final ProductMapper productMapper;

    @Override
    public Product getProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
    public Page<ProductResponse> getAllProducts(User user, int page, int size, String sortBy) {
        Pageable pageable = PageableUtil.create(page, size, sortBy);
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(product -> productMapper.toResponse(product, user));
    }

    @Override
    public ProductResponse getProductById(User user, Long id) {
        Product product = getProductEntityById(id);
        return productMapper.toResponse(product,user);
    }

    @Override
    public Page<ProductResponse> searchAndFilterProducts(
            User user,
            String name,
            List<Long> categoryIds,
            Double minPrice, Double maxPrice,
            int page,
            int size,
            String sortBy) {
        if (categoryIds != null && !categoryIds.isEmpty()) {
            long count = categoryRepository.countByIdIn(categoryIds);
            if (count != categoryIds.size()) {
                throw new BadRequestException(ErrorCode.CATEGORY_NOT_EMPTY);
            }
        }

        Specification<Product> spec = null;
        if (name != null && !name.isEmpty()) {
            spec = (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        }
        if (categoryIds != null && !categoryIds.isEmpty()) {
            Specification<Product> categorySpec = (root, query, cb) -> root.get("category").get("id").in(categoryIds);
            spec = spec == null ? categorySpec : spec.and(categorySpec);
        }
        if (minPrice != null) {
            Specification<Product> minSpec = (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
            spec = spec == null ? minSpec : spec.and(minSpec);
        }
        if (maxPrice != null) {
            Specification<Product> maxSpec = (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
            spec = spec == null ? maxSpec : spec.and(maxSpec);
        }

        Pageable pageable = PageableUtil.create(page, size, sortBy);

        Page<Product> productPage = (spec == null)
                ? productRepository.findAll(pageable)
                : productRepository.findAll(spec, pageable);

        return productPage.map(product -> {
            if (user instanceof Customer customer) {
                return productMapper.toResponse(product, customer);
            }
            return productMapper.toResponse(product);
        });
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ProductResponse updateProductStatus(Long id) {
        Product product = getProductEntityById(id);
        if (product.getStatus() == EProductStatus.AVAILABLE) {
            product.setStatus(EProductStatus.UNAVAILABLE);
        } else if (product.getStatus() == EProductStatus.UNAVAILABLE) {
            product.setStatus(EProductStatus.AVAILABLE);
        } else {
            throw new BadRequestException(ErrorCode.CANNOT_CHANGE_STATUS_OF_PRODUCT);
        }
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product();
        product.setStatus(EProductStatus.UNAVAILABLE);
        return saveAndReturnResponse(product, request);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = getProductEntityById(id);
        return saveAndReturnResponse(product, request);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProductById(Long id) {
        Product product = getProductEntityById(id);
        product.setStatus(EProductStatus.DELETED);
        productRepository.save(product);
    }

    private ProductResponse saveAndReturnResponse(Product product, ProductRequest request) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BadRequestException(ErrorCode.CATEGORY_NOT_FOUND));
            product.setCategory(category);
        }
        handleImages(product, request.getImageUrls());
        handleTags(product, request.getTags());
        return productMapper.toResponse(productRepository.save(product));
    }

    private void handleTags(Product product, List<String> tagNames) {
        if (tagNames == null) {
            product.getTags().clear();
            return;
        }
        // 1. Tìm các tag đã tồn tại
        List<String> distinctNames = tagNames.stream().distinct().toList();
        List<ProductTag> existingTags = productTagRepository.findAllByNameIn(distinctNames);
        Set<String> existingTagNames = existingTags.stream()
                .map(ProductTag::getName)
                .collect(Collectors.toSet());
        // 2. Tạo tag mới (chỉ lấy những cái chưa có trong DB)
        List<ProductTag> newTags = distinctNames.stream()
                .filter(name -> !existingTagNames.contains(name))
                .map(name -> ProductTag.builder().name(name).build())
                .toList();
        if (!newTags.isEmpty()) {
            productTagRepository.saveAll(newTags);
        }
        // 3. Cập nhật quan hệ
        product.getTags().clear();
        product.getTags().addAll(existingTags);
        product.getTags().addAll(newTags);
    }

    private void handleImages(Product product, List<String> urls) {
        if (product.getImages() == null) {
            product.setImages(new ArrayList<>());
        }
        product.getImages().clear();
        if (urls != null && !urls.isEmpty()) {
            List<ProductImage> newImages = urls.stream()
                    .map(url -> ProductImage.builder()
                            .url(url)
                            .product(product)
                            .name("Product Image")
                            .build())
                    .collect(Collectors.toList());
            product.getImages().addAll(newImages);
        }
    }
}
