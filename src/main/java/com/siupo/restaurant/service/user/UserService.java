package com.siupo.restaurant.service.user;

import com.siupo.restaurant.dto.AddressDTO;
import com.siupo.restaurant.dto.request.ChangePasswordRequest;
import com.siupo.restaurant.dto.request.UserRequest;
import com.siupo.restaurant.model.User;

import java.util.List;

public interface UserService {
    User getUserByEmail(String email);
    User getCurrentUserInfo(User user);
    User updateUserInfo(User user, UserRequest request);
    //Address
    List<AddressDTO> getUserAddresses(User user);
    AddressDTO addAddress(User user, AddressDTO addressDTO);
    AddressDTO updateAddress(User user, Long addressId, AddressDTO addressDTO);
    void deleteAddress(User user, Long addressId);
    //Password
    void changePassword(User user, ChangePasswordRequest request);
}
