package com.siupo.restaurant.service.product;

import com.siupo.restaurant.dto.ProductDTO;
import com.siupo.restaurant.dto.ReviewDTO;
import com.siupo.restaurant.enums.EProductStatus;
import com.siupo.restaurant.repository.CategoryRepository;
import com.siupo.restaurant.exception.ResourceNotFoundException;
import com.siupo.restaurant.model.Product;
import com.siupo.restaurant.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

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
                .map(img -> img.getUrl())
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
        dto.setStatus(product.getStatus());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }

    @Override
    public Page<ProductDTO> getAllProducts(int page, int size, String sortBy) {
        Sort sort;
        if (sortBy != null && sortBy.contains(",")) {
            String[] sortParts = sortBy.split(",");
            String field = sortParts[0];
            String direction = sortParts[1];
            sort = Sort.by(Sort.Direction.fromString(direction), field);
        } else {
            sort = Sort.by(Sort.Direction.ASC, sortBy != null ? sortBy : "id"); // Mặc định id,asc
        }
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findAll(pageable).map(this::toDTO);
    }

    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return toDTO(product);
    }
    @Override
    public Page<ProductDTO> searchAndFilterProducts(String name, List<Long> categoryIds, Double minPrice, Double maxPrice, int page, int size, String sortBy) {
        // Kiểm tra categoryIds
        if (categoryIds != null && !categoryIds.isEmpty()) {
            for (Long categoryId : categoryIds) {
                if (!categoryRepository.existsById(categoryId)) {
                    throw new IllegalArgumentException("Danh mục không tồn tại với id: " + categoryId);
                }
            }
        }

        // Khởi tạo Specification mà không dùng where(null)
        Specification<Product> spec = null;

        if (name != null && !name.isEmpty()) {
            spec = (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        }
        if (categoryIds != null && !categoryIds.isEmpty()) {
            Specification<Product> categorySpec = (root, query, cb) -> root.get("category").get("id").in(categoryIds);
            spec = spec == null ? categorySpec : spec.and(categorySpec);
        }
        if (minPrice != null) {
            Specification<Product> minPriceSpec = (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
            spec = spec == null ? minPriceSpec : spec.and(minPriceSpec);
        }
        if (maxPrice != null) {
            Specification<Product> maxPriceSpec = (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
            spec = spec == null ? maxPriceSpec : spec.and(maxPriceSpec);
        }

        Sort sort;
        if (sortBy != null && sortBy.contains(",")) {
            String[] sortParts = sortBy.split(",");
            String field = sortParts[0];
            String direction = sortParts[1];
            sort = Sort.by(Sort.Direction.fromString(direction), field);
        } else {
            sort = Sort.by(Sort.Direction.ASC, sortBy != null ? sortBy : "id");
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        // Nếu spec là null, trả về tất cả sản phẩm với phân trang
        return spec == null ? productRepository.findAll(pageable).map(this::toDTO)
                : productRepository.findAll(spec, pageable).map(this::toDTO);
    }

        @Override
        public Product getProductEntityById(Long id) {
            return productRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        }
}