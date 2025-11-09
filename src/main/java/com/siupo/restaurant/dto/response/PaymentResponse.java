package com.siupo.restaurant.dto.response;

import com.siupo.restaurant.enums.EPaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Long id;
    private EPaymentMethod method;
    private String status;
    private Double amount;
    private LocalDateTime paidAt;
}