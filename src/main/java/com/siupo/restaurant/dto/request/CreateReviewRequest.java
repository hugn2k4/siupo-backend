package com.siupo.restaurant.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReviewRequest {
    
    @NotNull(message = "Order item ID is required")
    private Long orderItemId;
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;
    
    @Size(min = 10, max = 2000, message = "Content must be between 10 and 2000 characters")
    private String content;
    
    @Size(max = 5, message = "Maximum 5 images allowed")
    private List<String> imageUrls;
}
