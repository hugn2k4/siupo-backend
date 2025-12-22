package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.ShopDataResponse;
import com.siupo.restaurant.service.page.PageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/page")
@RequiredArgsConstructor
public class PageController {
    private final PageService pageService;

    @GetMapping("/shop/initial-data")
    public ResponseEntity<ApiResponse<ShopDataResponse>> getShopInitialData() {
        ShopDataResponse dataResponse = pageService.getInitialDataShop();
        ApiResponse<ShopDataResponse> response = ApiResponse.<ShopDataResponse>builder()
                .success(true)
                .code("200")
                .message("Shop initial data retrieved successfully")
                .data(dataResponse)
                .build();
        return ResponseEntity.ok(response);
    }
}
