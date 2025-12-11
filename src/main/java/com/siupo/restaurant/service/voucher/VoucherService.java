package com.siupo.restaurant.service.voucher;

import com.siupo.restaurant.dto.VoucherDTO;
import com.siupo.restaurant.dto.request.ApplyVoucherRequest;
import com.siupo.restaurant.dto.response.VoucherDiscountResponse;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.model.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VoucherService {
    
    // ========== Customer APIs ==========
    List<VoucherDTO> getAvailableVouchers(User user);
    VoucherDiscountResponse validateAndCalculateDiscount(ApplyVoucherRequest request, User user);
    VoucherDTO getVoucherByCode(String code, User user);
    
    // ========== Admin APIs ==========
    VoucherDTO createVoucher(VoucherDTO voucherDTO);
    VoucherDTO updateVoucher(Long id, VoucherDTO voucherDTO);
    void deleteVoucher(Long id);
    Page<VoucherDTO> getAllVouchers(Pageable pageable);
    VoucherDTO getVoucherById(Long id);
    VoucherDTO toggleVoucherStatus(Long id);
    
    // ========== Internal use ==========
    void recordVoucherUsage(Voucher voucher, User user, Long orderId, Double discountAmount);
    boolean canUserUseVoucher(Voucher voucher, User user);
    Voucher getVoucherEntityByCode(String code);
    void updateExpiredVouchers();
}
