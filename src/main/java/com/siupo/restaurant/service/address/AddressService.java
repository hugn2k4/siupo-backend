package com.siupo.restaurant.service.address;

import com.siupo.restaurant.dto.AddressDTO;
import com.siupo.restaurant.dto.response.AddressResponse;
import com.siupo.restaurant.model.User;

import java.util.List;

public interface AddressService {
    AddressResponse addAddress(User user, AddressDTO dto);
    AddressResponse updateAddress(User user, Long id, AddressDTO dto);
    void deleteAddress(User user, Long id);
    AddressResponse setDefaultAddress(User user, Long id);
    AddressResponse getDefaultAddress(User user);
    List<AddressResponse> getAddresses(User user);
}
