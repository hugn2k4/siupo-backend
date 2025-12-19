package com.siupo.restaurant.service.review;

import com.siupo.restaurant.dto.request.CreateReviewRequest;
import com.siupo.restaurant.dto.response.OrderReviewsResponse;
import com.siupo.restaurant.dto.response.ReviewResponse;
import com.siupo.restaurant.enums.EOrderStatus;
import com.siupo.restaurant.exception.base.ErrorCode;
import com.siupo.restaurant.exception.business.BadRequestException;
import com.siupo.restaurant.mapper.ReviewMapper;
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
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request, User user) {
        if (request.getOrderItemId() == null) {
            throw new BadRequestException(ErrorCode.ORDER_ITEM_NOT_FOUND);
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new BadRequestException(ErrorCode.STAR_RATING_OUT_OF_BOUNDS);
        }
        OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new BadRequestException(ErrorCode.ORDER_ITEM_NOT_FOUND));
        if (orderItem.getOrder().getUser() == null || 
            !orderItem.getOrder().getUser().getId().equals(user.getId())) {
                throw new BadRequestException(ErrorCode.CANNOT_REVIEW_OWN_ORDER);
        }
        if (orderItem.getOrder().getStatus() != EOrderStatus.DELIVERED &&
            orderItem.getOrder().getStatus() != EOrderStatus.COMPLETED) {
                throw new BadRequestException(ErrorCode.CANNOT_REVIEW_BEFORE_DELIVERY);
        }
        if (orderItem.getReviewed()) {
            throw new BadRequestException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }
        Review review = Review.builder()
                .orderItem(orderItem)
                .product(orderItem.getProduct())
                .user(user)
                .rate(request.getRating().doubleValue())
                .content(request.getContent())
                .build();
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
        Review savedReview = reviewRepository.save(review);
        orderItem.setReviewed(true);
        orderItemRepository.save(orderItem);
        return reviewMapper.toResponse(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse getReviewByOrderItemId(Long orderItemId, User user) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ORDER_ITEM_NOT_FOUND));
        // Check if user owns this order
        if (orderItem.getOrder().getUser() == null || 
            !orderItem.getOrder().getUser().getId().equals(user.getId())) {
            throw new BadRequestException(ErrorCode.CANNOT_REVIEW_OWN_ORDER);
        }
        // Get review for this order item
        Review review = reviewRepository.findByOrderItemId(orderItemId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.REVIEW_NOT_FOUND));
        return reviewMapper.toResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderReviewsResponse getReviewsByOrderId(Long orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.ORDER_NOT_FOUND));
        // Check if user owns this order
        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException(ErrorCode.CANNOT_REVIEW_OWN_ORDER);
        }
        // Get all reviews for this order
        List<Review> reviews = reviewRepository.findByOrderId(orderId);
        // Map to response
        List<ReviewResponse> reviewResponses = reviews.stream()
                .map(reviewMapper::toResponse)
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
        productRepository.findById(productId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.PRODUCT_NOT_FOUND));
        // Get all reviews for this product
        List<Review> reviews = reviewRepository.findByProductId(productId);
        // Map to response
        return reviews.stream()
                .map(reviewMapper::toResponse)
                .collect(Collectors.toList());
    }
}
