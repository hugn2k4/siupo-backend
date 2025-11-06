package com.siupo.restaurant.dto.response;

import com.siupo.restaurant.enums.EOrderStatus;
import com.siupo.restaurant.enums.EPaymentMethod;
import com.siupo.restaurant.model.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long orderId;
    private List<OrderItemResponse> items;
    private Double totalPrice;
    private Double shippingFee;
    private Double vat;
    private EOrderStatus status;
    private Payment payment;
    private EPaymentMethod paymentMethod;
}
