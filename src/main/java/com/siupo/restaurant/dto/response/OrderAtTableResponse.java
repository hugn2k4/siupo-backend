package com.siupo.restaurant.dto.response;
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
    private LocalDateTime createdAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemResponse {
        private Long itemId;
        private Long productId;
        private String productName;
        private Long quantity;
        private Double price;
        private Double subtotal;
    }
}
