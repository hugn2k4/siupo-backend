package com.siupo.restaurant.dto.response;

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
public class VoucherResponse {
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
    private Boolean isAvailable;
    private Integer userUsageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
