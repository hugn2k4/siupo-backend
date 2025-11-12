package com.siupo.restaurant.dto;

import com.siupo.restaurant.enums.EProductStatus;
import com.siupo.restaurant.model.Product;
import com.siupo.restaurant.model.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Long categoryId;
    private String categoryName;
    private List<ImageDTO> images;
    private List<String> imageUrls;
    private List<ReviewDTO> reviews;
    private EProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductDTO toDTO(Product product) {
        if (product == null) return null;

        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .images(product.getImages() != null ? product.getImages().stream()
                        .map(image -> new ImageDTO(image.getId(), image.getUrl(), image.getName()))
                        .toList() : null)
                .imageUrls(product.getImages() != null ? product.getImages().stream()
                        .map(ProductImage::getUrl)
                        .toList() : null)
                .reviews(product.getReviews() != null ? product.getReviews().stream()
                        .map(review -> {
                            ReviewDTO dto = new ReviewDTO();
                            dto.setId(review.getId());
                            dto.setContent(review.getContent());
                            dto.setRate(review.getRate());
                            dto.setCreatedAt(review.getCreatedAt());
                            dto.setUpdatedAt(review.getUpdatedAt());
                            dto.setUserId(review.getUser() != null ? review.getUser().getId() : null);
                            return dto;
                        })
                        .toList() : null)
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}