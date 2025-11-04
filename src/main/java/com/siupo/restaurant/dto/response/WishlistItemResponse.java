package com.siupo.restaurant.dto.response;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productDescription;
    private Double productPrice;
    private List<String> productImages; // ✅ đổi sang list
}
