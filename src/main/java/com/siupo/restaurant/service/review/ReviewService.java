package com.siupo.restaurant.service.review;

import com.siupo.restaurant.dto.request.CreateReviewRequest;
import com.siupo.restaurant.dto.response.OrderReviewsResponse;
import com.siupo.restaurant.dto.response.ReviewResponse;
import com.siupo.restaurant.model.User;

import java.util.List;

public interface ReviewService {
    
    ReviewResponse createReview(CreateReviewRequest request, User user);
    
    ReviewResponse getReviewByOrderItemId(Long orderItemId, User user);
    
    OrderReviewsResponse getReviewsByOrderId(Long orderId, User user);
    
    List<ReviewResponse> getReviewsByProductId(Long productId);
}
