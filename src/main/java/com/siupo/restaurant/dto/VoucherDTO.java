package com.siupo.restaurant.dto;

import com.siupo.restaurant.enums.EVoucherStatus;
import com.siupo.restaurant.enums.EVoucherType;
import com.siupo.restaurant.model.Voucher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private EVoucherType type;
    private Double discountValue;
    private Double minOrderValue;
    private Double maxDiscountAmount;
    private Integer usageLimit;
    private Integer usedCount;
    private Integer usageLimitPerUser;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private EVoucherStatus status;
    private Boolean isPublic;
    private Boolean isAvailable; // Người dùng có thể dùng không
    private Integer userUsageCount; // Số lần user này đã dùng
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static VoucherDTO toDTO(Voucher voucher) {
        if (voucher == null) return null;
        
        return VoucherDTO.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .name(voucher.getName())
                .description(voucher.getDescription())
                .type(voucher.getType())
                .discountValue(voucher.getDiscountValue())
                .minOrderValue(voucher.getMinOrderValue())
                .maxDiscountAmount(voucher.getMaxDiscountAmount())
                .usageLimit(voucher.getUsageLimit())
                .usedCount(voucher.getUsedCount())
                .usageLimitPerUser(voucher.getUsageLimitPerUser())
                .startDate(voucher.getStartDate())
                .endDate(voucher.getEndDate())
                .status(voucher.getStatus())
                .isPublic(voucher.getIsPublic())
                .createdAt(voucher.getCreatedAt())
                .updatedAt(voucher.getUpdatedAt())
                .build();
    }
}
