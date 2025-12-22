package com.siupo.restaurant.mapper;

import com.siupo.restaurant.dto.response.AddressResponse;
import com.siupo.restaurant.model.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {
    public AddressResponse toAddressResponse(Address address) {
        return base(address, false);
    }

    public AddressResponse toDefaultAddressResponse(Address address) {
        return base(address, true);
    }

    private AddressResponse base(Address address, boolean isDefault) {
        return AddressResponse.builder()
                .id(address.getId())
                .address(address.getAddress())
                .ward(address.getWard())
                .district(address.getDistrict())
                .province(address.getProvince())
                .receiverName(address.getReceiverName())
                .receiverPhone(address.getReceiverPhone())
                .isDefault(isDefault)
                .build();
    }
}
