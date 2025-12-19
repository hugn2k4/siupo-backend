package com.siupo.restaurant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherDiscountResponse {
    private Long voucherId;
    private String voucherCode;
    private String voucherName;
    private Double discountAmount;
    private Double finalAmount;
    private String message;
}
