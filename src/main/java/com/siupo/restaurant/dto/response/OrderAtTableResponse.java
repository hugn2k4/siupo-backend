package com.siupo.restaurant.dto.response;

import com.siupo.restaurant.enums.EOrderStatus;
import com.siupo.restaurant.enums.EPaymentMethod;
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
public class OrderAtTableResponse {
    private Long orderId;
    private Long tableId;
    private String tableName;
    private List<OrderItemResponse> items;
    private Double totalAmount;
    private EOrderStatus status;
    private EPaymentMethod paymentMethod;
    private Boolean isPaid;
    private String payUrl; // URL thanh toán MoMo (nếu có)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
