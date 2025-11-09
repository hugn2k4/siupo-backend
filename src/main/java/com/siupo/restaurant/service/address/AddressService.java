package com.siupo.restaurant.service.address;

import com.siupo.restaurant.dto.AddressDTO;
import com.siupo.restaurant.dto.request.AddressUpdateRequest;
import com.siupo.restaurant.dto.response.AddressResponse;
import com.siupo.restaurant.dto.response.MessageDataReponse;
import com.siupo.restaurant.model.User;

import java.util.List;

public interface AddressService {
    List<AddressResponse> getAddresses(User user);
    AddressResponse addAddress(User user, AddressDTO address);
    AddressResponse updateAddress(Long addressId, AddressDTO address);
    MessageDataReponse deleteAddress(User user, Long addressId);
    AddressResponse getAddressDefault(User user);
    AddressResponse setAddressDefault(User user, Long addressId);
}
