package com.siupo.restaurant.service.user;

import com.siupo.restaurant.dto.AddressDTO;
import com.siupo.restaurant.dto.request.ChangePasswordRequest;
import com.siupo.restaurant.dto.request.UserRequest;
import com.siupo.restaurant.enums.EUserStatus;
import com.siupo.restaurant.model.User;

import java.util.List;

public interface UserService {
    User getUserByEmail(String email);
    User getCurrentUserInfo(User user);
    User updateUserInfo(User user, UserRequest request);
    void changePassword(User user, ChangePasswordRequest request);
    List<User> getAllCustomers();
    void updateCustomerStatus(Long customerId, EUserStatus status);
}
