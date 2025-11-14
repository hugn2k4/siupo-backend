package com.siupo.restaurant.dto;

import com.siupo.restaurant.enums.EOrderStatus;
import com.siupo.restaurant.enums.EPaymentMethod;
import com.siupo.restaurant.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

    private Long orderId;
    private EOrderStatus status;
    private Double totalPrice;
    private Double shippingFee;
    private Double vat;
    private EPaymentMethod paymentMethod;
    private List<OrderItemDTO> items;

    public static OrderDTO toDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(OrderItemDTO::toDTO)
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .shippingFee(order.getShippingFee())
                .vat(order.getVat())
                .paymentMethod(order.getPayment() != null ? order.getPayment().getPaymentMethod() : null)
                .items(itemDTOs)
                .build();
    }
}
