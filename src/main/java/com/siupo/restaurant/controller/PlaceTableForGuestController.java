package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.request.PlaceTableForGuestRequest;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.PlaceTableForGuestResponse;
import com.siupo.restaurant.service.PlaceTableForGuest.PlaceTableForGuestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/place-table-for-guest")
@RequiredArgsConstructor
public class PlaceTableForGuestController {

    private final PlaceTableForGuestService placeTableForGuestService;

    @PostMapping("/place-table")
    public ResponseEntity<ApiResponse<PlaceTableForGuestResponse>> createPlaceTableRequest(
            @Valid @RequestBody PlaceTableForGuestRequest request) {

        PlaceTableForGuestResponse response = placeTableForGuestService.createPlaceTableRequest(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<PlaceTableForGuestResponse>builder()
                        .success(true)
                        .message("Yêu cầu đặt bàn đã được gửi thành công")
                        .data(response)
                        .build());
    }
}