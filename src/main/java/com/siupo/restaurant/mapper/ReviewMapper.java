package com.siupo.restaurant.mapper;

import com.siupo.restaurant.dto.response.ReviewResponse;
import com.siupo.restaurant.model.Review;
import com.siupo.restaurant.model.ReviewImage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReviewMapper {
    public ReviewResponse toResponse(Review review) {
        List<String> imageUrls = review.getImages() != null
                ? review.getImages().stream()
                .map(ReviewImage::getUrl)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return ReviewResponse.builder()
                .id(review.getId())
                .orderItemId(review.getOrderItem().getId())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .rating(review.getRate())
                .content(review.getContent())
                .imageUrls(imageUrls)
                .userName(review.getUser() != null ? review.getUser().getFullName() : "Anonymous")
                .userId(review.getUser() != null ? review.getUser().getId() : null)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
