package com.siupo.restaurant.service.product;

import com.siupo.restaurant.dto.ProductDTO;
import com.siupo.restaurant.dto.ReviewDTO;
import com.siupo.restaurant.dto.request.ProductRequest;
import com.siupo.restaurant.dto.response.ProductResponse;
import com.siupo.restaurant.enums.EProductStatus;
import com.siupo.restaurant.exception.base.ErrorCode;
import com.siupo.restaurant.exception.business.BadRequestException;
import com.siupo.restaurant.model.Category;
import com.siupo.restaurant.model.ProductImage;
import com.siupo.restaurant.model.ProductTag;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.repository.CategoryRepository;
import com.siupo.restaurant.repository.ProductTagRepository;
import com.siupo.restaurant.model.Product;
import com.siupo.restaurant.repository.ProductRepository;
import com.siupo.restaurant.repository.WishlistRepository;
import com.siupo.restaurant.service.wishlist.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductTagRepository productTagRepository;

    @Autowired
    private WishlistService wishlistService;
    @Autowired
    private WishlistRepository wishlistRepository;

    // ----------------------- CREATE PAGEABLE -----------------------
    private Pageable createPageable(int page, int size, String sortBy) {
        Sort sort;
        if (sortBy != null && sortBy.contains(",")) {
            String[] parts = sortBy.split(",");
            String field = parts[0].trim();
            String direction = parts.length > 1 ? parts[1].trim() : "asc";
            sort = Sort.by(Sort.Direction.fromString(direction), field);
        } else {
            String field = sortBy != null ? sortBy.trim() : "id";
            sort = Sort.by(Sort.Direction.ASC, field);
        }
        return PageRequest.of(page, size, sort);
    }

    // ----------------------- GET ALL WITH WISHLIST -----------------------
    public Page<ProductDTO> getAllProductsWithWishlist(User user, int page, int size, String sortBy) {
        Pageable pageable = createPageable(page, size, sortBy);
        Page<Product> products = productRepository.findAll(pageable);

        Long userId = user != null ? user.getId() : null;
        return products.map(product -> toDTOWithWishlist(product, userId));
    }

    public ProductDTO toDTOWithWishlist(Product product, Long userId) {
        ProductDTO dto = toDTO(product);
        if (userId != null) {
            boolean isInWishlist = wishlistService.isProductInWishlist(userId, product.getId());
            dto.setWishlist(isInWishlist);
        } else {
            dto.setWishlist(false);
        }
        return dto;
    }

    private ProductDTO toDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        dto.setImageUrls(product.getImages().stream()
                .map(ProductImage::getUrl)
                .collect(Collectors.toList()));
        dto.setReviews(product.getReviews().stream().map(review -> {
            ReviewDTO reviewDTO = new ReviewDTO();
            reviewDTO.setId(review.getId());
            reviewDTO.setContent(review.getContent());
            reviewDTO.setRate(review.getRate());
            reviewDTO.setCreatedAt(review.getCreatedAt());
            reviewDTO.setUpdatedAt(review.getUpdatedAt());
            if (review.getUser() != null) {
                reviewDTO.setUserId(review.getUser().getId());
            } else {
                reviewDTO.setUserId(null);
            }
            return reviewDTO;
        }).collect(Collectors.toList()));
        
        // Map tags
        if (product.getTags() != null && !product.getTags().isEmpty()) {
            dto.setTags(product.getTags().stream()
                    .map(ProductTag::getName)
                    .collect(Collectors.toList()));
        }
        
        dto.setStatus(product.getStatus());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }

    // ----------------------- BASIC GET -----------------------
    @Override
    public Page<ProductDTO> getAllProducts(int page, int size, String sortBy) {
        Pageable pageable = createPageable(page, size, sortBy);
        return productRepository.findAll(pageable).map(this::toDTO);
    }

    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(ErrorCode.LOI_CHUA_DAT));
//                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return toDTO(product);
    }

    // ----------------------- SEARCH AND FILTER -----------------------
    @Override
    public Page<ProductDTO> searchAndFilterProducts(String name, List<Long> categoryIds,
                                                    Double minPrice, Double maxPrice,
                                                    int page, int size, String sortBy) {
        if (categoryIds != null && !categoryIds.isEmpty()) {
            for (Long categoryId : categoryIds) {
                if (!categoryRepository.existsById(categoryId)) {
                    throw new IllegalArgumentException("Danh mục không tồn tại với id: " + categoryId);
                }
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

        Pageable pageable = createPageable(page, size, sortBy);
        return spec == null
                ? productRepository.findAll(pageable).map(this::toDTO)
                : productRepository.findAll(spec, pageable).map(this::toDTO);
    }

    // ----------------------- ENTITY GET -----------------------
    @Override
    public Product getProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(ErrorCode.LOI_CHUA_DAT));
//                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    // ----------------------- DELETE -----------------------
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteProductById(Long id) {
        Product product = getProductEntityById(id);
        product.setStatus(EProductStatus.DELETED);
        productRepository.save(product);
    }

    // ----------------------- UPDATE STATUS -----------------------
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ProductDTO updateProductStatus(Long id) {
        Product product = getProductEntityById(id);
        if (product.getStatus() == EProductStatus.AVAILABLE) {
            product.setStatus(EProductStatus.UNAVAILABLE);
        } else if (product.getStatus() == EProductStatus.UNAVAILABLE) {
            product.setStatus(EProductStatus.AVAILABLE);
        } else {
            throw new IllegalStateException("Cannot change status of a deleted product");
        }
        return toDTO(productRepository.save(product));
    }

    // ----------------------- CREATE -----------------------
    @Override
    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .status(EProductStatus.UNAVAILABLE)
                .build();

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<ProductImage> images = request.getImageUrls().stream()
                    .map(url -> ProductImage.builder()
                            .url(url)
                            .name("Product Image")
                            .product(product)
                            .build())
                    .collect(Collectors.toList());
            product.setImages(images);
        }

        if (request.getTags() != null && !request.getTags().isEmpty()) {
            for (String tagName : request.getTags()) {
                ProductTag tag = productTagRepository.findByName(tagName)
                        .orElseGet(() -> {
                            ProductTag newTag = ProductTag.builder()
                                    .name(tagName)
                                    .build();
                            return productTagRepository.save(newTag);
                        });
                product.getTags().add(tag);
            }
        }

        productRepository.save(product);
        return mapToResponse(product);
    }

    // ----------------------- UPDATE -----------------------
    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = getProductEntityById(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        if (request.getImageUrls() != null) {
            product.getImages().clear();
            List<ProductImage> newImages = request.getImageUrls().stream()
                    .map(url -> ProductImage.builder()
                            .url(url)
                            .name("Product Image")
                            .product(product)
                            .build())
                    .collect(Collectors.toList());
            product.getImages().addAll(newImages);
        }

        if (request.getTags() != null) {
            product.getTags().clear();
            for (String tagName : request.getTags()) {
                ProductTag tag = productTagRepository.findByName(tagName)
                        .orElseGet(() -> {
                            ProductTag newTag = ProductTag.builder()
                                    .name(tagName)
                                    .build();
                            return productTagRepository.save(newTag);
                        });
                product.getTags().add(tag);
            }
        }

        productRepository.save(product);
        return mapToResponse(product);
    }

    private ProductResponse mapToResponse(Product product) {
        List<String> urls = product.getImages() == null ? List.of()
                : product.getImages().stream().map(ProductImage::getUrl).toList();

        Long categoryId = product.getCategory() != null ? product.getCategory().getId() : null;
        String categoryName = product.getCategory() != null ? product.getCategory().getName() : null;

        List<String> tags = product.getTags() == null ? List.of()
                : product.getTags().stream().map(ProductTag::getName).toList();

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .imageUrls(urls)
                .categoryId(categoryId)
                .categoryName(categoryName)
                .tags(tags)
                .build();
    }
}
