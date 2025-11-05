package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.request.PlaceTableRequest;
import com.siupo.restaurant.dto.response.PlaceTableResponse;
import com.siupo.restaurant.enums.EPlaceTableStatus;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.service.placeTableForCustomer.PlaceTableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/place-tables")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PlaceTableForCustomerController {

    private final PlaceTableService placeTableService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPlaceTable(
            @Valid @RequestBody PlaceTableRequest request) {
        PlaceTableResponse response = placeTableService.createPlaceTable(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Đặt bàn thành công! Nhà hàng sẽ liên hệ xác nhận sớm nhất.",
                "data", response
        ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlaceTableResponse> getPlaceTableById(@PathVariable Long id) {
        PlaceTableResponse response = placeTableService.getPlaceTableById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-booking/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PlaceTableResponse> getMyPlaceTableById(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        PlaceTableResponse response = placeTableService.getPlaceTableByIdAndUserId(id, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PlaceTableResponse>> getAllPlaceTables() {
        List<PlaceTableResponse> response = placeTableService.getAllPlaceTables();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-bookings")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<PlaceTableResponse>> getMyPlaceTables(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<PlaceTableResponse> response = placeTableService.getPlaceTablesByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PlaceTableResponse>> getPlaceTablesByStatus(@PathVariable String status) {
        EPlaceTableStatus tableStatus = EPlaceTableStatus.valueOf(status.toUpperCase());
        List<PlaceTableResponse> response = placeTableService.getPlaceTablesByStatus(tableStatus);
        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlaceTableResponse> updatePlaceTableStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String status = request.get("status");
        String note = request.get("note");
        PlaceTableResponse response = placeTableService.updatePlaceTableStatus(id, status, note);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{placeTableId}/items")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PlaceTableResponse> addItemToPlaceTable(
            @PathVariable Long placeTableId,
            @RequestBody Map<String, Object> request) {
        Long productId = Long.valueOf(request.get("productId").toString());
        Long quantity = Long.valueOf(request.get("quantity").toString());
        String note = request.get("note") != null ? request.get("note").toString() : null;

        PlaceTableResponse response = placeTableService.addItemToPlaceTable(
                placeTableId, productId, quantity, note);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{placeTableId}/items/{orderItemId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PlaceTableResponse> removeItemFromPlaceTable(
            @PathVariable Long placeTableId,
            @PathVariable Long orderItemId) {
        PlaceTableResponse response = placeTableService.removeItemFromPlaceTable(placeTableId, orderItemId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> cancelPlaceTable(@PathVariable Long id) {
        placeTableService.cancelPlaceTable(id);
        return ResponseEntity.ok(Map.of(
                "success", "true",
                "message", "Hủy đơn đặt bàn thành công"
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deletePlaceTable(@PathVariable Long id) {
        placeTableService.deletePlaceTable(id);
        return ResponseEntity.ok(Map.of(
                "success", "true",
                "message", "Xóa đơn đặt bàn thành công"
        ));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PlaceTableResponse>> getPlaceTablesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<PlaceTableResponse> response = placeTableService.getPlaceTablesByDateRange(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> countPlaceTablesByStatus(@PathVariable String status) {
        EPlaceTableStatus tableStatus = EPlaceTableStatus.valueOf(status.toUpperCase());
        Long count = placeTableService.countPlaceTablesByStatus(tableStatus);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PatchMapping("/{id}/total-price")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PlaceTableResponse> updateTotalPrice(@PathVariable Long id) {
        PlaceTableResponse response = placeTableService.updateTotalPrice(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/resend-confirmation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> resendConfirmation(@PathVariable Long id) {
        placeTableService.sendBookingConfirmation(id);
        return ResponseEntity.ok(Map.of(
                "success", "true",
                "message", "Đã gửi lại thông báo xác nhận"
        ));
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        return ((User) authentication.getPrincipal()).getId();
    }
}