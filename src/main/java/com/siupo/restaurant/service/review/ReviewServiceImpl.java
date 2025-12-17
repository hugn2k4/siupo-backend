package com.siupo.restaurant.service.review;

import com.siupo.restaurant.dto.request.CreateReviewRequest;
import com.siupo.restaurant.dto.response.OrderReviewsResponse;
import com.siupo.restaurant.dto.response.ReviewResponse;
import com.siupo.restaurant.enums.EOrderStatus;
import com.siupo.restaurant.exception.base.ErrorCode;
import com.siupo.restaurant.exception.business.BadRequestException;
import com.siupo.restaurant.model.*;
import com.siupo.restaurant.repository.OrderItemRepository;
import com.siupo.restaurant.repository.OrderRepository;
import com.siupo.restaurant.repository.ProductRepository;
import com.siupo.restaurant.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request, User user) {
        // Validate request
        if (request.getOrderItemId() == null) {
            throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
//            throw new BadRequestException("Order item ID is required");
        }
        
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
//            throw new BadRequestException("Rating must be between 1 and 5");
        }

        // Get order item
        OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.LOI_CHUA_DAT));
//                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

        // Check if user owns this order
        if (orderItem.getOrder().getUser() == null || 
            !orderItem.getOrder().getUser().getId().equals(user.getId())) {
                throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
//            throw new UnauthorizedException("You can only review your own orders");
        }

        // Check if order is delivered or completed
        if (orderItem.getOrder().getStatus() != EOrderStatus.DELIVERED &&
            orderItem.getOrder().getStatus() != EOrderStatus.COMPLETED) {
                throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
//            throw new BadRequestException("You can only review delivered or completed orders");
        }

        // Check if already reviewed
        if (orderItem.getReviewed()) {
            throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
//            throw new BadRequestException("This order item has already been reviewed");
        }

        // Create review
        Review review = Review.builder()
                .orderItem(orderItem)
                .product(orderItem.getProduct())
                .user(user)
                .rate(request.getRating().doubleValue())
                .content(request.getContent())
                .build();

        // Add images if provided
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<ReviewImage> images = request.getImageUrls().stream()
                    .map(url -> ReviewImage.builder()
                            .url(url)
                            .review(review)
                            .build())
                    .collect(Collectors.toList());
            review.setImages(images);
        } else {
            review.setImages(new ArrayList<>());
        }

        // Save review
        Review savedReview = reviewRepository.save(review);

        // Mark order item as reviewed
        orderItem.setReviewed(true);
        orderItemRepository.save(orderItem);

        log.info("Review created successfully for order item #{} by user #{}", 
                orderItem.getId(), user.getId());

        return mapToReviewResponse(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReviewByOrderItemId(Long orderItemId, User user) {
        // Get order item
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.LOI_CHUA_DAT));
//                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

        // Check if user owns this order
        if (orderItem.getOrder().getUser() == null || 
            !orderItem.getOrder().getUser().getId().equals(user.getId())) {
            throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
//            throw new UnauthorizedException("You can only view reviews for your own orders");
        }

        // Get review for this order item
        Review review = reviewRepository.findByOrderItemId(orderItemId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.LOI_CHUA_DAT));
//                .orElseThrow(() -> new ResourceNotFoundException("Review not found for this order item"));

        return mapToReviewResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderReviewsResponse getReviewsByOrderId(Long orderId, User user) {
        // Get order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.LOI_CHUA_DAT));
//                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Check if user owns this order
        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
//            throw new UnauthorizedException("You can only view reviews for your own orders");
        }

        // Get all reviews for this order
        List<Review> reviews = reviewRepository.findByOrderId(orderId);

        // Map to response
        List<ReviewResponse> reviewResponses = reviews.stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());

        // Count total and reviewed items
        int totalItems = order.getItems().size();
        int reviewedItems = (int) order.getItems().stream()
                .filter(OrderItem::getReviewed)
                .count();

        return OrderReviewsResponse.builder()
                .orderId(orderId)
                .reviews(reviewResponses)
                .totalItems(totalItems)
                .reviewedItems(reviewedItems)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProductId(Long productId) {
        // Verify product exists
        productRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.LOI_CHUA_DAT));
//                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Get all reviews for this product
        List<Review> reviews = reviewRepository.findByProductId(productId);

        // Map to response
        return reviews.stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
    }

    private ReviewResponse mapToReviewResponse(Review review) {
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
