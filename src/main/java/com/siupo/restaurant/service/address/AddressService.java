package com.siupo.restaurant.service.address;

import com.siupo.restaurant.dto.AddressDTO;
import com.siupo.restaurant.dto.request.AddressUpdateRequest;
import com.siupo.restaurant.model.User;

import java.util.List;

public interface AddressService {
    List<AddressDTO> getAddresses(User user);
    AddressDTO addAddress(User user, AddressDTO dto);
    AddressDTO updateAddressByContent(User user, AddressDTO dto);
    void deleteAddressByContent(User user, AddressDTO dto);
    AddressDTO setDefaultAddressByContent(User user, AddressDTO dto);
    AddressDTO getDefaultAddress(User user);
    AddressDTO updateAddressByOldAndNew(User user, AddressUpdateRequest request);
}
