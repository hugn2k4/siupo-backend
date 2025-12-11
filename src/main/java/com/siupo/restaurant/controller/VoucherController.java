package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.VoucherDTO;
import com.siupo.restaurant.dto.request.ApplyVoucherRequest;
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
@CrossOrigin(origins = "*", maxAge = 3600)
public class VoucherController {

    private final VoucherService voucherService;

    // ========== Customer APIs ==========

    /**
     * Get all available vouchers for the current user
     */
    @GetMapping("/available")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<VoucherDTO>> getAvailableVouchers(
            @AuthenticationPrincipal User user) {
        List<VoucherDTO> vouchers = voucherService.getAvailableVouchers(user);
        return ResponseEntity.ok(vouchers);
    }

    /**
     * Validate voucher and calculate discount
     */
    @PostMapping("/validate")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<VoucherDiscountResponse> validateVoucher(
            @Valid @RequestBody ApplyVoucherRequest request,
            @AuthenticationPrincipal User user) {
        VoucherDiscountResponse response = voucherService.validateAndCalculateDiscount(request, user);
        return ResponseEntity.ok(response);
    }

    /**
     * Get voucher details by code
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<VoucherDTO> getVoucherByCode(
            @PathVariable String code,
            @AuthenticationPrincipal User user) {
        VoucherDTO voucher = voucherService.getVoucherByCode(code, user);
        return ResponseEntity.ok(voucher);
    }

    // ========== Admin APIs ==========

    /**
     * Create new voucher (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VoucherDTO> createVoucher(@Valid @RequestBody VoucherDTO voucherDTO) {
        VoucherDTO created = voucherService.createVoucher(voucherDTO);
        return ResponseEntity.ok(created);
    }

    /**
     * Update existing voucher (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VoucherDTO> updateVoucher(
            @PathVariable Long id,
            @Valid @RequestBody VoucherDTO voucherDTO) {
        VoucherDTO updated = voucherService.updateVoucher(id, voucherDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete voucher (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all vouchers with pagination (Admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<VoucherDTO>> getAllVouchers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<VoucherDTO> vouchers = voucherService.getAllVouchers(pageable);
        return ResponseEntity.ok(vouchers);
    }

    /**
     * Get voucher by ID (Admin only)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VoucherDTO> getVoucherById(@PathVariable Long id) {
        VoucherDTO voucher = voucherService.getVoucherById(id);
        return ResponseEntity.ok(voucher);
    }

    /**
     * Toggle voucher status (Admin only)
     */
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VoucherDTO> toggleVoucherStatus(@PathVariable Long id) {
        VoucherDTO voucher = voucherService.toggleVoucherStatus(id);
        return ResponseEntity.ok(voucher);
    }
}
