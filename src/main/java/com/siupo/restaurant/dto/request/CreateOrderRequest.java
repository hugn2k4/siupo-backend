package com.siupo.restaurant.dto.request;

import com.siupo.restaurant.dto.AddressDTO;
import com.siupo.restaurant.dto.CartItemDTO;
import com.siupo.restaurant.enums.EPaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotNull(message = "Địa chỉ giao hàng không được để trống")
    private AddressDTO shippingAddress;

    private EPaymentMethod paymentMethod = EPaymentMethod.COD;

    private List<CartItemDTO> items;
    
    private String voucherCode;
}
