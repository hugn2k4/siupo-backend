package com.siupo.restaurant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderReviewsResponse {
    private Long orderId;
    private List<ReviewResponse> reviews;
    private Integer totalItems;
    private Integer reviewedItems;
}
