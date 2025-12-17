package com.siupo.restaurant.service.voucher;

import com.siupo.restaurant.dto.VoucherDTO;
import com.siupo.restaurant.dto.request.ApplyVoucherRequest;
import com.siupo.restaurant.dto.response.VoucherDiscountResponse;
import com.siupo.restaurant.enums.EVoucherStatus;
import com.siupo.restaurant.exception.base.ErrorCode;
import com.siupo.restaurant.exception.business.BadRequestException;
import com.siupo.restaurant.exception.business.NotFoundException;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.model.Voucher;
import com.siupo.restaurant.model.VoucherUsage;
import com.siupo.restaurant.repository.OrderRepository;
import com.siupo.restaurant.repository.VoucherRepository;
import com.siupo.restaurant.repository.VoucherUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final OrderRepository orderRepository;

    // ========== Public APIs (No auth required) ==========

    @Override
    @Transactional(readOnly = true)
    public List<VoucherDTO> getPublicVouchers() {
        LocalDateTime now = LocalDateTime.now();
        List<Voucher> vouchers = voucherRepository.findAvailableVouchers(EVoucherStatus.ACTIVE, now);

        return vouchers.stream()
                .filter(Voucher::getIsPublic)
                .map(voucher -> {
                    VoucherDTO dto = VoucherDTO.toDTO(voucher);
                    dto.setIsAvailable(true);
                    dto.setUserUsageCount(0);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ========== Customer APIs ==========

    @Override
    @Transactional(readOnly = true)
    public List<VoucherDTO> getAvailableVouchers(User user) {
        LocalDateTime now = LocalDateTime.now();
        List<Voucher> vouchers = voucherRepository.findAvailableVouchers(EVoucherStatus.ACTIVE, now);
        
        return vouchers.stream()
                .map(voucher -> {
                    VoucherDTO dto = VoucherDTO.toDTO(voucher);
                    dto.setIsAvailable(canUserUseVoucher(voucher, user));
                    dto.setUserUsageCount((int) voucherUsageRepository.countByVoucherAndUser(voucher, user));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherDiscountResponse validateAndCalculateDiscount(ApplyVoucherRequest request, User user) {
        Voucher voucher = getVoucherEntityByCode(request.getVoucherCode());
        
        // Validate voucher
        validateVoucher(voucher, user, request.getOrderAmount());
        
        // Calculate discount
        double discountAmount = calculateDiscount(voucher, request.getOrderAmount());
        double finalAmount = request.getOrderAmount() - discountAmount;
        
        return VoucherDiscountResponse.builder()
                .voucherId(voucher.getId())
                .voucherCode(voucher.getCode())
                .voucherName(voucher.getName())
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .message("Voucher áp dụng thành công!")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherDTO getVoucherByCode(String code, User user) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException(ErrorCode.VOUCHER_NOT_FOUND));
        
        VoucherDTO dto = VoucherDTO.toDTO(voucher);
        dto.setIsAvailable(canUserUseVoucher(voucher, user));
        dto.setUserUsageCount((int) voucherUsageRepository.countByVoucherAndUser(voucher, user));
        
        return dto;
    }

    // ========== Admin APIs ==========

    @Override
    @Transactional
    public VoucherDTO createVoucher(VoucherDTO voucherDTO) {
        // Check code uniqueness
        if (voucherRepository.findByCode(voucherDTO.getCode()).isPresent()) {
            throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
//            throw new BadRequestException("Mã voucher đã tồn tại: " + voucherDTO.getCode());
        }
        
        // Validate dates
        if (voucherDTO.getStartDate().isAfter(voucherDTO.getEndDate())) {
            throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
//            throw new BadRequestException("Ngày bắt đầu phải trước ngày kết thúc");
        }
        
        Voucher voucher = Voucher.builder()
                .code(voucherDTO.getCode().toUpperCase())
                .name(voucherDTO.getName())
                .description(voucherDTO.getDescription())
                .type(voucherDTO.getType())
                .discountValue(voucherDTO.getDiscountValue())
                .minOrderValue(voucherDTO.getMinOrderValue())
                .maxDiscountAmount(voucherDTO.getMaxDiscountAmount())
                .usageLimit(voucherDTO.getUsageLimit() != null ? voucherDTO.getUsageLimit() : 0)
                .usedCount(0)
                .usageLimitPerUser(voucherDTO.getUsageLimitPerUser())
                .startDate(voucherDTO.getStartDate())
                .endDate(voucherDTO.getEndDate())
                .status(voucherDTO.getStatus() != null ? voucherDTO.getStatus() : EVoucherStatus.ACTIVE)
                .isPublic(voucherDTO.getIsPublic() != null ? voucherDTO.getIsPublic() : true)
                .build();
        
        voucher = voucherRepository.save(voucher);
        log.info("Created voucher: {}", voucher.getCode());
        
        return VoucherDTO.toDTO(voucher);
    }

    @Override
    @Transactional
    public VoucherDTO updateVoucher(Long id, VoucherDTO voucherDTO) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.LOI_CHUA_DAT));
//                .orElseThrow(() -> new NotFoundException("Không tìm thấy voucher với ID: " + id));
        
        // Check code uniqueness if changed
        if (!voucher.getCode().equals(voucherDTO.getCode())) {
            if (voucherRepository.findByCode(voucherDTO.getCode()).isPresent()) {
                throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
//                throw new BadRequestException("Mã voucher đã tồn tại: " + voucherDTO.getCode());
            }
            voucher.setCode(voucherDTO.getCode().toUpperCase());
        }
        
        // Validate dates
        if (voucherDTO.getStartDate().isAfter(voucherDTO.getEndDate())) {
            throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
//            throw new BadRequestException("Ngày bắt đầu phải trước ngày kết thúc");
        }
        
        voucher.setName(voucherDTO.getName());
        voucher.setDescription(voucherDTO.getDescription());
        voucher.setType(voucherDTO.getType());
        voucher.setDiscountValue(voucherDTO.getDiscountValue());
        voucher.setMinOrderValue(voucherDTO.getMinOrderValue());
        voucher.setMaxDiscountAmount(voucherDTO.getMaxDiscountAmount());
        voucher.setUsageLimit(voucherDTO.getUsageLimit());
        voucher.setUsageLimitPerUser(voucherDTO.getUsageLimitPerUser());
        voucher.setStartDate(voucherDTO.getStartDate());
        voucher.setEndDate(voucherDTO.getEndDate());
        voucher.setStatus(voucherDTO.getStatus());
        voucher.setIsPublic(voucherDTO.getIsPublic());
        
        voucher = voucherRepository.save(voucher);
        log.info("Updated voucher: {}", voucher.getCode());
        
        return VoucherDTO.toDTO(voucher);
    }

    @Override
    @Transactional
    public void deleteVoucher(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.LOI_CHUA_DAT));
//                .orElseThrow(() -> new NotFoundException("Không tìm thấy voucher với ID: " + id));
        
        // Soft delete by setting status to EXPIRED
        voucher.setStatus(EVoucherStatus.EXPIRED);
        voucherRepository.save(voucher);
        
        log.info("Deleted voucher: {}", voucher.getCode());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherDTO> getAllVouchers(Pageable pageable) {
        Page<Voucher> vouchers = voucherRepository.findAll(pageable);
        return vouchers.map(VoucherDTO::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherDTO getVoucherById(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.LOI_CHUA_DAT));
//                .orElseThrow(() -> new NotFoundException("Không tìm thấy voucher với ID: " + id));
        return VoucherDTO.toDTO(voucher);
    }

    @Override
    @Transactional
    public VoucherDTO toggleVoucherStatus(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.LOI_CHUA_DAT));
//                .orElseThrow(() -> new NotFoundException("Không tìm thấy voucher với ID: " + id));
        
        if (voucher.getStatus() == EVoucherStatus.ACTIVE) {
            voucher.setStatus(EVoucherStatus.INACTIVE);
        } else if (voucher.getStatus() == EVoucherStatus.INACTIVE) {
            voucher.setStatus(EVoucherStatus.ACTIVE);
        }
        
        voucher = voucherRepository.save(voucher);
        log.info("Toggled voucher status: {} -> {}", voucher.getCode(), voucher.getStatus());
        
        return VoucherDTO.toDTO(voucher);
    }

    // ========== Internal Methods ==========

    @Override
    @Transactional
    public void recordVoucherUsage(Voucher voucher, User user, Long orderId, Double discountAmount) {
        VoucherUsage usage = VoucherUsage.builder()
                .voucher(voucher)
                .user(user)
                .order(orderId != null ? orderRepository.findById(orderId).orElse(null) : null)
                .discountAmount(discountAmount)
                .build();
        
        voucherUsageRepository.save(usage);
        
        // Increment used count
        voucher.setUsedCount(voucher.getUsedCount() + 1);
        voucherRepository.save(voucher);
        
        log.info("Recorded voucher usage: {} by user {}", voucher.getCode(), user.getId());
    }

    @Override
    public boolean canUserUseVoucher(Voucher voucher, User user) {
        // Check if voucher is active
        if (voucher.getStatus() != EVoucherStatus.ACTIVE) {
            return false;
        }
        
        // Check date validity
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(voucher.getStartDate()) || now.isAfter(voucher.getEndDate())) {
            return false;
        }
        
        // Check total usage limit
        if (voucher.getUsageLimit() > 0 && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            return false;
        }
        
        // Check per-user usage limit
        if (voucher.getUsageLimitPerUser() != null && voucher.getUsageLimitPerUser() > 0) {
            long userUsageCount = voucherUsageRepository.countByVoucherAndUser(voucher, user);
            if (userUsageCount >= voucher.getUsageLimitPerUser()) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Voucher getVoucherEntityByCode(String code) {
        return voucherRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new NotFoundException(ErrorCode.VOUCHER_NOT_FOUND));
//                .orElseThrow(() -> new NotFoundException("Không tìm thấy voucher với mã: " + code));
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 0 * * *") // Run daily at midnight
    public void updateExpiredVouchers() {
        LocalDateTime now = LocalDateTime.now();
        List<Voucher> expiredVouchers = voucherRepository.findExpiredVouchers(now, EVoucherStatus.EXPIRED);
        
        for (Voucher voucher : expiredVouchers) {
            voucher.setStatus(EVoucherStatus.EXPIRED);
        }
        
        if (!expiredVouchers.isEmpty()) {
            voucherRepository.saveAll(expiredVouchers);
            log.info("Updated {} expired vouchers", expiredVouchers.size());
        }
    }

    // ========== Private Helper Methods ==========

    private void validateVoucher(Voucher voucher, User user, Double orderAmount) {
        // Check if user can use voucher
        if (!canUserUseVoucher(voucher, user)) {
            throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
//            throw new BadRequestException("Bạn không thể sử dụng voucher này");
        }
        
        // Check minimum order value
        if (voucher.getMinOrderValue() != null && orderAmount < voucher.getMinOrderValue()) {
//            throw new BadRequestException(
//                String.format("Đơn hàng tối thiểu phải từ %.0f VND để sử dụng voucher này",
//                    voucher.getMinOrderValue())
//            );
            throw new BadRequestException(ErrorCode.LOI_CHUA_DAT);
        }
    }

    private double calculateDiscount(Voucher voucher, Double orderAmount) {
        double discount = 0.0;
        
        switch (voucher.getType()) {
            case PERCENTAGE:
                discount = orderAmount * (voucher.getDiscountValue() / 100.0);
                // Apply max discount limit if exists
                if (voucher.getMaxDiscountAmount() != null && discount > voucher.getMaxDiscountAmount()) {
                    discount = voucher.getMaxDiscountAmount();
                }
                break;
                
            case FIXED_AMOUNT:
                discount = voucher.getDiscountValue();
                // Don't exceed order amount
                if (discount > orderAmount) {
                    discount = orderAmount;
                }
                break;
                
            case FREE_SHIPPING:
                discount = 0.0; // Shipping will be handled in order service
                break;
        }
        
        return Math.round(discount * 100.0) / 100.0;
    }
}
