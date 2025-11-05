package com.siupo.restaurant.dto.response;
import lombok.*;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistResponse {
    private Long id;
    private Long userId;
    private List<WishlistItemResponse> items;
    private Integer totalItems;
}