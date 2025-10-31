package com.siupo.restaurant.service.address;

import com.siupo.restaurant.dto.AddressDTO;
import com.siupo.restaurant.model.User;

import java.util.List;

public interface AddressService {
    List<AddressDTO> getAddresses(User user);
    AddressDTO addAddress(User user, AddressDTO dto);
    AddressDTO updateAddress(User user, Long id, AddressDTO dto);
    void deleteAddress(User user, Long id);
    AddressDTO setDefaultAddress(User user, Long id);
    AddressDTO getDefaultAddress(User user);

}
