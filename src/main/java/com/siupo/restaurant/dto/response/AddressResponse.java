package com.siupo.restaurant.dto.response;

import com.siupo.restaurant.model.Address;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {
    private Long id;
    private String address;
    private String ward;
    private String district;
    private String province;
    private String receiverName;
    private String receiverPhone;
    private Boolean isDefault;

    public static AddressResponse mapAddressEntityToResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .address(address.getAddress())
                .ward(address.getWard())
                .district(address.getDistrict())
                .province(address.getProvince())
                .receiverName(address.getReceiverName())
                .receiverPhone(address.getReceiverPhone())
                .build();
    }
}