package com.siupo.restaurant.service.voucher;

import com.siupo.restaurant.dto.response.VoucherResponse;
import com.siupo.restaurant.dto.request.ApplyVoucherRequest;
import com.siupo.restaurant.dto.response.VoucherDiscountResponse;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.model.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VoucherService {
    
    // ========== Public APIs (No auth required) ==========
    List<VoucherResponse> getPublicVouchers();
    
    // ========== Customer APIs ==========
    List<VoucherResponse> getAvailableVouchers(User user);
    VoucherDiscountResponse validateAndCalculateDiscount(ApplyVoucherRequest request, User user);
    VoucherResponse getVoucherByCode(String code, User user);
    
    // ========== Admin APIs ==========
    VoucherResponse createVoucher(VoucherResponse voucherResponse);
    VoucherResponse updateVoucher(Long id, VoucherResponse voucherResponse);
    void deleteVoucher(Long id);
    Page<VoucherResponse> getAllVouchers(Pageable pageable);
    VoucherResponse getVoucherById(Long id);
    VoucherResponse toggleVoucherStatus(Long id);
    
    // ========== Internal use ==========
    void recordVoucherUsage(Voucher voucher, User user, Long orderId, Double discountAmount);
    boolean canUserUseVoucher(Voucher voucher, User user);
    Voucher getVoucherEntityByCode(String code);
    void updateExpiredVouchers();
}
