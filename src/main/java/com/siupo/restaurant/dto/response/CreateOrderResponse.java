package com.siupo.restaurant.dto.response;

import com.siupo.restaurant.enums.EOrderStatus;
import com.siupo.restaurant.enums.EPaymentMethod;
import com.siupo.restaurant.model.Payment;
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
    private List<OrderItemResponse> items;
    private Double totalPrice;
    private Double shippingFee;
    private Double vat;
    private EOrderStatus status;
    private EPaymentMethod paymentMethod;
    private String payUrl; // URL thanh toán MoMo (nếu chọn MoMo)
    private String qrCodeUrl; // QR Code URL từ MoMo (nếu có)
    private String deeplink; // Deeplink để mở app MoMo (nếu có)
}
