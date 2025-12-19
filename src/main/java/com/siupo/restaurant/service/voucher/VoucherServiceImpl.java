package com.siupo.restaurant.service.voucher;

import com.siupo.restaurant.dto.response.VoucherResponse;
import com.siupo.restaurant.dto.request.ApplyVoucherRequest;
import com.siupo.restaurant.dto.response.VoucherDiscountResponse;
import com.siupo.restaurant.enums.EVoucherStatus;
import com.siupo.restaurant.exception.base.ErrorCode;
import com.siupo.restaurant.exception.business.BadRequestException;
import com.siupo.restaurant.exception.business.NotFoundException;
import com.siupo.restaurant.mapper.VoucherMapper;
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
    private final VoucherMapper voucherMapper;

    // ========== Public APIs (No auth required) ==========

    @Override
    @Transactional(readOnly = true)
    public List<VoucherResponse> getPublicVouchers() {
        LocalDateTime now = LocalDateTime.now();
        List<Voucher> vouchers = voucherRepository.findAvailableVouchers(EVoucherStatus.ACTIVE, now);
        return vouchers.stream()
                .filter(Voucher::getIsPublic)
                .map(voucher -> {
                    VoucherResponse dto = voucherMapper.toDto(voucher);
                    dto.setIsAvailable(true);
                    dto.setUserUsageCount(0);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ========== Customer APIs ==========

    @Override
    @Transactional(readOnly = true)
    public List<VoucherResponse> getAvailableVouchers(User user) {
        LocalDateTime now = LocalDateTime.now();
        List<Voucher> vouchers = voucherRepository.findAvailableVouchers(EVoucherStatus.ACTIVE, now);
        return vouchers.stream()
                .map(voucher -> {
                    VoucherResponse dto = voucherMapper.toDto(voucher);
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
    public VoucherResponse getVoucherByCode(String code, User user) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException(ErrorCode.VOUCHER_NOT_FOUND));
        VoucherResponse dto = voucherMapper.toDto(voucher);
        dto.setIsAvailable(canUserUseVoucher(voucher, user));
        dto.setUserUsageCount((int) voucherUsageRepository.countByVoucherAndUser(voucher, user));
        return dto;
    }

    // ========== Admin APIs ==========

    @Override
    @Transactional
    public VoucherResponse createVoucher(VoucherResponse voucherResponse) {
        if (voucherRepository.findByCode(voucherResponse.getCode()).isPresent()) {
            throw new BadRequestException(ErrorCode.VOUCHER_ALREADY_EXISTS);
        }
        if (voucherResponse.getStartDate().isAfter(voucherResponse.getEndDate())) {
            throw new BadRequestException(ErrorCode.VOUCHER_START_DATE_INVALID);
        }
        Voucher voucher = Voucher.builder()
                .code(voucherResponse.getCode().toUpperCase())
                .name(voucherResponse.getName())
                .description(voucherResponse.getDescription())
                .type(voucherResponse.getType())
                .discountValue(voucherResponse.getDiscountValue())
                .minOrderValue(voucherResponse.getMinOrderValue())
                .maxDiscountAmount(voucherResponse.getMaxDiscountAmount())
                .usageLimit(voucherResponse.getUsageLimit() != null ? voucherResponse.getUsageLimit() : 0)
                .usedCount(0)
                .usageLimitPerUser(voucherResponse.getUsageLimitPerUser())
                .startDate(voucherResponse.getStartDate())
                .endDate(voucherResponse.getEndDate())
                .status(voucherResponse.getStatus() != null ? voucherResponse.getStatus() : EVoucherStatus.ACTIVE)
                .isPublic(voucherResponse.getIsPublic() != null ? voucherResponse.getIsPublic() : true)
                .build();
        return voucherMapper.toDto(voucherRepository.save(voucher));
    }

    @Override
    @Transactional
    public VoucherResponse updateVoucher(Long id, VoucherResponse voucherResponse) {
        Voucher voucher = voucherRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorCode.VOUCHER_NOT_FOUND));
        // Check code uniqueness if changed
        if (!voucher.getCode().equals(voucherResponse.getCode())) {
            if (voucherRepository.findByCode(voucherResponse.getCode()).isPresent()) {
                throw new BadRequestException(ErrorCode.VOUCHER_ALREADY_EXISTS);
            }
            voucher.setCode(voucherResponse.getCode().toUpperCase());
        }
        // Validate dates
        if (voucherResponse.getStartDate().isAfter(voucherResponse.getEndDate())) {
            throw new BadRequestException(ErrorCode.VOUCHER_START_DATE_INVALID);
        }
        voucher.setName(voucherResponse.getName());
        voucher.setDescription(voucherResponse.getDescription());
        voucher.setType(voucherResponse.getType());
        voucher.setDiscountValue(voucherResponse.getDiscountValue());
        voucher.setMinOrderValue(voucherResponse.getMinOrderValue());
        voucher.setMaxDiscountAmount(voucherResponse.getMaxDiscountAmount());
        voucher.setUsageLimit(voucherResponse.getUsageLimit());
        voucher.setUsageLimitPerUser(voucherResponse.getUsageLimitPerUser());
        voucher.setStartDate(voucherResponse.getStartDate());
        voucher.setEndDate(voucherResponse.getEndDate());
        voucher.setStatus(voucherResponse.getStatus());
        voucher.setIsPublic(voucherResponse.getIsPublic());
        return voucherMapper.toDto(voucherRepository.save(voucher));
    }

    @Override
    @Transactional
    public void deleteVoucher(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.VOUCHER_NOT_FOUND));
        voucher.setStatus(EVoucherStatus.EXPIRED);
        voucherRepository.save(voucher);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherResponse> getAllVouchers(Pageable pageable) {
        Page<Voucher> vouchers = voucherRepository.findAll(pageable);
        return vouchers.map(voucherMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherResponse getVoucherById(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.VOUCHER_NOT_FOUND));
        return voucherMapper.toDto(voucher);
    }

    @Override
    @Transactional
    public VoucherResponse toggleVoucherStatus(Long id) {
        Voucher voucher = voucherRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorCode.VOUCHER_NOT_FOUND));
        if (voucher.getStatus() == EVoucherStatus.ACTIVE) {
            voucher.setStatus(EVoucherStatus.INACTIVE);
        } else if (voucher.getStatus() == EVoucherStatus.INACTIVE) {
            voucher.setStatus(EVoucherStatus.ACTIVE);
        }
        return voucherMapper.toDto(voucherRepository.save(voucher));
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
        voucher.setUsedCount(voucher.getUsedCount() + 1);
        voucherRepository.save(voucher);
    }

    @Override
    public boolean canUserUseVoucher(Voucher voucher, User user) {
        if (voucher.getStatus() != EVoucherStatus.ACTIVE) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(voucher.getStartDate()) || now.isAfter(voucher.getEndDate())) {
            return false;
        }
        // Check total usage limit
        if (voucher.getUsageLimit() > 0 && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            return false;
        }
        // Check per-user usage limit
        if (voucher.getUsageLimitPerUser() != null && voucher.getUsageLimitPerUser() > 0
            && voucherUsageRepository.countByVoucherAndUser(voucher, user) >= voucher.getUsageLimitPerUser()) {
                return false;
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Voucher getVoucherEntityByCode(String code) {
        return voucherRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new NotFoundException(ErrorCode.VOUCHER_NOT_FOUND));
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
            throw new BadRequestException(ErrorCode.YOU_CANNOT_APPLY_VOUCHER);
        }
        // Check minimum order value
        if (voucher.getMinOrderValue() != null && orderAmount < voucher.getMinOrderValue()) {
            throw new BadRequestException(ErrorCode.CANNOT_APPLY_VOUCHER);
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
                discount = 0.0;
                break;
        }
        return Math.round(discount * 100.0) / 100.0;
    }
}
