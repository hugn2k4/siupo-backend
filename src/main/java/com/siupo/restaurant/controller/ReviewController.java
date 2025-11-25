package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.request.CreateReviewRequest;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.OrderReviewsResponse;
import com.siupo.restaurant.dto.response.ReviewResponse;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.service.review.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateReviewRequest request) {

        ReviewResponse response = reviewService.createReview(request, user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ReviewResponse>builder()
                        .code("201")
                        .success(true)
                        .message("Review created successfully")
                        .data(response)
                        .build());
    }

    @GetMapping("/order-items/{orderItemId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewByOrderItemId(
            @AuthenticationPrincipal User user,
            @PathVariable Long orderItemId) {

        ReviewResponse response = reviewService.getReviewByOrderItemId(orderItemId, user);

        return ResponseEntity.ok(
                ApiResponse.<ReviewResponse>builder()
                        .code("200")
                        .success(true)
                        .message("Review retrieved successfully")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderReviewsResponse>> getReviewsByOrderId(
            @AuthenticationPrincipal User user,
            @PathVariable Long orderId) {

        OrderReviewsResponse response = reviewService.getReviewsByOrderId(orderId, user);

        return ResponseEntity.ok(
                ApiResponse.<OrderReviewsResponse>builder()
                        .code("200")
                        .success(true)
                        .message("Reviews retrieved successfully")
                        .data(response)
                        .build()
        );
    }
}
