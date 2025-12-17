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
public class ShopDataResponse {
    private List<ComboResponse> combos;
    private List<CategoryResponse> categories;
    private List<ProductResponse> products;
    private List<TagResponse> tags;
    private List<ProductWithRatingResponse> latestProducts;
}
