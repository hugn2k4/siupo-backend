package com.siupo.restaurant.service.user;

import com.siupo.restaurant.dto.request.ChangePasswordRequest;
import com.siupo.restaurant.dto.request.UserRequest;
import com.siupo.restaurant.dto.response.UserResponse;
import com.siupo.restaurant.enums.EUserStatus;
import com.siupo.restaurant.model.User;

import java.util.List;

public interface UserService {
    UserResponse getCurrentCustomerInfo(User user);
    UserResponse updateCustomerInfo(User user, UserRequest request);
    List<UserResponse> getAllCustomers();
    void changePassword(User user, ChangePasswordRequest request);
    void updateCustomerStatus(Long customerId, EUserStatus status);
    //AUTHENTICATION
    User getUserByEmail(String email);
}
