package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.request.PlaceTableForCustomerRequest;
import com.siupo.restaurant.dto.response.PlaceTableForCustomerResponse;
import com.siupo.restaurant.service.placeTableForCustomer.PlaceTableForCustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/place-tables")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlaceTableForCustomerController {

    private final PlaceTableForCustomerService placeTableForCustomerService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPlaceTable(
            @Valid @RequestBody PlaceTableForCustomerRequest request) {
        PlaceTableForCustomerResponse response = placeTableForCustomerService.createPlaceTable(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Đặt bàn thành công! Nhà hàng sẽ liên hệ xác nhận sớm nhất.",
                "data", response
        ));
    }
}