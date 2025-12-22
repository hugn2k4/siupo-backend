package com.siupo.restaurant.dto.response;

import com.siupo.restaurant.dto.OrderItemDTO;
import com.siupo.restaurant.enums.EOrderStatus;
import com.siupo.restaurant.enums.EPaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderResponse {
    private Long orderId;
    private EOrderStatus status;
    private Double totalPrice;
    private Double shippingFee;
    private Double vat;
    private EPaymentMethod paymentMethod;
    private List<OrderItemDTO> items;
    private String payUrl;
    private String qrCodeUrl;
    private String deeplink;
    private String voucherCode;
    private Double discountAmount;
    private Double finalAmount;
}
