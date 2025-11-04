package com.siupo.restaurant.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistRequest {
    @NotNull(message = "Product ID is required")
    private Long productId;
}
