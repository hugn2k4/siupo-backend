package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.response.VoucherResponse;
import com.siupo.restaurant.dto.request.ApplyVoucherRequest;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.VoucherDiscountResponse;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.service.voucher.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {
    private final VoucherService voucherService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> getPublicVouchers() {
        List<VoucherResponse> vouchers = voucherService.getPublicVouchers();
        return ResponseEntity.ok(ApiResponse.<List<VoucherResponse>>builder()
                .success(true)
                .code("200")
                .message("Public vouchers retrieved successfully")
                .data(vouchers)
                .build());
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<VoucherResponse>>> getAvailableVouchers(
            @AuthenticationPrincipal User user) {
        List<VoucherResponse> vouchers = voucherService.getAvailableVouchers(user);
        return ResponseEntity.ok(ApiResponse.<List<VoucherResponse>>builder()
                .success(true)
                .code("200")
                .message("Available vouchers retrieved successfully")
                .data(vouchers)
                .build());
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<VoucherDiscountResponse>> validateVoucher(
            @Valid @RequestBody ApplyVoucherRequest request,
            @AuthenticationPrincipal User user) {
        VoucherDiscountResponse response = voucherService.validateAndCalculateDiscount(request, user);
        return ResponseEntity.ok(ApiResponse.<VoucherDiscountResponse>builder()
                .success(true)
                .code("200")
                .message("Voucher validated successfully")
                .data(response)
                .build());
    }

    /**
     * Get voucher details by code
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<VoucherResponse>> getVoucherByCode(
            @PathVariable String code,
            @AuthenticationPrincipal User user) {
        VoucherResponse voucher = voucherService.getVoucherByCode(code, user);
        return ResponseEntity.ok(ApiResponse.<VoucherResponse>builder()
                .success(true)
                .code("200")
                .message("Voucher retrieved successfully")
                .data(voucher)
                .build());
    }

    // ========== Admin APIs ==========
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VoucherResponse>> createVoucher(@Valid @RequestBody VoucherResponse voucherResponse) {
        VoucherResponse created = voucherService.createVoucher(voucherResponse);
        return ResponseEntity.ok(ApiResponse.<VoucherResponse>builder()
                .success(true)
                .code("201")
                .message("Voucher created successfully")
                .data(created)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VoucherResponse>> updateVoucher(
            @PathVariable Long id,
            @Valid @RequestBody VoucherResponse voucherResponse) {
        VoucherResponse updated = voucherService.updateVoucher(id, voucherResponse);
        return ResponseEntity.ok(ApiResponse.<VoucherResponse>builder()
                .success(true)
                .code("200")
                .message("Voucher updated successfully")
                .data(updated)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .code("200")
                .message("Voucher deleted successfully")
                .build());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<VoucherResponse>>> getAllVouchers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<VoucherResponse> vouchers = voucherService.getAllVouchers(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<VoucherResponse>>builder()
                .success(true)
                .code("200")
                .message("Vouchers retrieved successfully")
                .data(vouchers)
                .build());
    }

    @PostMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VoucherResponse>> getVoucherById(@PathVariable Long id) {
        VoucherResponse voucher = voucherService.getVoucherById(id);
        return ResponseEntity.ok(ApiResponse.<VoucherResponse>builder()
                .success(true)
                .code("200")
                .message("Voucher retrieved successfully")
                .data(voucher)
                .build());
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VoucherResponse>> toggleVoucherStatus(@PathVariable Long id) {
        VoucherResponse voucher = voucherService.toggleVoucherStatus(id);
        return ResponseEntity.ok(ApiResponse.<VoucherResponse>builder()
                .success(true)
                .code("200")
                .message("Voucher status toggled successfully")
                .data(voucher)
                .build());
    }
}
