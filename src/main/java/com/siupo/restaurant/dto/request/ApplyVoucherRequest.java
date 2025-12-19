package com.siupo.restaurant.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyVoucherRequest {
    @NotBlank(message = "Mã voucher không được để trống")
    private String voucherCode;
    
    private Double orderAmount;
}
