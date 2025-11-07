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
    private Long addressId;
    private AddressDTO updateAddress;
}