package com.siupo.restaurant.dto.request;

import com.siupo.restaurant.enums.EPaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAtTableRequest {

    @NotNull(message = "ID bàn không được để trống")
    private Long tableId;

    @NotEmpty(message = "Danh sách món ăn không được để trống")
    private List<OrderItemRequest> items;

    private String note;

    private EPaymentMethod paymentMethod; // MOMO hoặc COD

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemRequest {

        private Long productId;

        private Long comboId;

        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        private Long quantity;

        private String note;
    }
}