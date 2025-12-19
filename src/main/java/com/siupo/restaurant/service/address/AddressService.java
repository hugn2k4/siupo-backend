package com.siupo.restaurant.service.address;

import com.siupo.restaurant.dto.request.AddressRequest;
import com.siupo.restaurant.dto.response.AddressResponse;
import com.siupo.restaurant.model.User;

import java.util.List;

public interface AddressService {
    List<AddressResponse> getAddresses(User user);
    AddressResponse addAddress(User user, AddressRequest address);
    AddressResponse updateAddress(User user, Long addressId, AddressRequest address);
    void deleteAddress(User user, Long addressId);
    AddressResponse getAddressDefault(User user);
    AddressResponse setAddressDefault(User user, Long addressId);
}
