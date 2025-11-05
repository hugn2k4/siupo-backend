package com.siupo.restaurant.dto.request;

import com.siupo.restaurant.dto.AddressDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressUpdateRequest {
    @NotNull(message = "Địa chỉ cũ không được để trống")
    private AddressDTO oldAddress;

    @NotNull(message = "Thông tin cập nhật không được để trống")
    @Valid
    private AddressDTO newAddress;
}