package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.response.PlaceTableForCustomerResponse;
import com.siupo.restaurant.dto.response.PlaceTableForGuestResponse;
import com.siupo.restaurant.service.managePlaceTable.ManagePlaceTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manage/place-tables")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class ManagePlaceTableController {

    private final ManagePlaceTableService managePlaceTableService;

    // ============= CUSTOMER BOOKINGS =============

    @GetMapping("/customers")
    public ResponseEntity<List<PlaceTableForCustomerResponse>> getAllCustomerBookings() {
        return ResponseEntity.ok(managePlaceTableService.getAllCustomerBookings());
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<PlaceTableForCustomerResponse> getCustomerBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(managePlaceTableService.getCustomerBookingById(id));
    }

    @GetMapping("/customers/status/{status}")
    public ResponseEntity<List<PlaceTableForCustomerResponse>> getCustomerBookingsByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(managePlaceTableService.getCustomerBookingsByStatus(status));
    }

    @GetMapping("/customers/date-range")
    public ResponseEntity<List<PlaceTableForCustomerResponse>> getCustomerBookingsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(managePlaceTableService.getCustomerBookingsByDateRange(startDate, endDate));
    }

    @PatchMapping("/customers/{id}/confirm")
    public ResponseEntity<PlaceTableForCustomerResponse> confirmCustomerBooking(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        String note = request != null ? request.get("note") : null;
        return ResponseEntity.ok(managePlaceTableService.confirmCustomerBooking(id, note));
    }

    @PatchMapping("/customers/{id}/deny")
    public ResponseEntity<PlaceTableForCustomerResponse> denyCustomerBooking(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        String note = request != null ? request.get("note") : null;
        return ResponseEntity.ok(managePlaceTableService.denyCustomerBooking(id, note));
    }

    @PatchMapping("/customers/{id}/complete")
    public ResponseEntity<PlaceTableForCustomerResponse> completeCustomerBooking(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        String note = request != null ? request.get("note") : null;
        return ResponseEntity.ok(managePlaceTableService.completeCustomerBooking(id, note));
    }

    @PatchMapping("/customers/{id}/status")
    public ResponseEntity<PlaceTableForCustomerResponse> updateCustomerBookingStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(managePlaceTableService.updateCustomerBookingStatus(
                id, request.get("status"), request.get("note")));
    }

    @GetMapping("/customers/count/status/{status}")
    public ResponseEntity<Map<String, Long>> countCustomerBookingsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(Map.of("count", managePlaceTableService.countCustomerBookingsByStatus(status)));
    }

    @PostMapping("/customers/{id}/resend-confirmation")
    public ResponseEntity<Map<String, String>> resendCustomerConfirmation(@PathVariable Long id) {
        managePlaceTableService.resendCustomerConfirmation(id);
        return ResponseEntity.ok(Map.of(
                "success", "true",
                "message", "Đã gửi lại thông báo xác nhận"
        ));
    }

    // ============= GUEST BOOKINGS =============

    @GetMapping("/guests")
    public ResponseEntity<List<PlaceTableForGuestResponse>> getAllGuestBookings() {
        return ResponseEntity.ok(managePlaceTableService.getAllGuestBookings());
    }

    @GetMapping("/guests/{id}")
    public ResponseEntity<PlaceTableForGuestResponse> getGuestBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(managePlaceTableService.getGuestBookingById(id));
    }

    @GetMapping("/guests/status/{status}")
    public ResponseEntity<List<PlaceTableForGuestResponse>> getGuestBookingsByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(managePlaceTableService.getGuestBookingsByStatus(status));
    }

    @GetMapping("/guests/phone/{phoneNumber}")
    public ResponseEntity<List<PlaceTableForGuestResponse>> getGuestBookingsByPhone(
            @PathVariable String phoneNumber) {
        return ResponseEntity.ok(managePlaceTableService.getGuestBookingsByPhone(phoneNumber));
    }

    @GetMapping("/guests/date-range")
    public ResponseEntity<List<PlaceTableForGuestResponse>> getGuestBookingsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(managePlaceTableService.getGuestBookingsByDateRange(startDate, endDate));
    }

    @PatchMapping("/guests/{id}/confirm")
    public ResponseEntity<PlaceTableForGuestResponse> confirmGuestBooking(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        String note = request != null ? request.get("note") : null;
        return ResponseEntity.ok(managePlaceTableService.confirmGuestBooking(id, note));
    }

    @PatchMapping("/guests/{id}/deny")
    public ResponseEntity<PlaceTableForGuestResponse> denyGuestBooking(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        String note = request != null ? request.get("note") : null;
        return ResponseEntity.ok(managePlaceTableService.denyGuestBooking(id, note));
    }

    @PatchMapping("/guests/{id}/complete")
    public ResponseEntity<PlaceTableForGuestResponse> completeGuestBooking(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        String note = request != null ? request.get("note") : null;
        return ResponseEntity.ok(managePlaceTableService.completeGuestBooking(id, note));
    }

    @PatchMapping("/guests/{id}/status")
    public ResponseEntity<PlaceTableForGuestResponse> updateGuestBookingStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(managePlaceTableService.updateGuestBookingStatus(
                id, request.get("status"), request.get("note")));
    }

    // ============= COMBINED STATISTICS =============

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getBookingStatistics() {
        return ResponseEntity.ok(managePlaceTableService.getBookingStatistics());
    }

    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodayBookings() {
        return ResponseEntity.ok(managePlaceTableService.getTodayBookings());
    }
}