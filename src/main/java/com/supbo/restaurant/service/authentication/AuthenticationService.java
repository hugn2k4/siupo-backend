package com.supbo.restaurant.service.authentication;

import com.supbo.restaurant.dto.request.UserLoginRequestDTO;
import com.supbo.restaurant.dto.request.UserRegisterRequestDTO;
import com.supbo.restaurant.dto.response.UserLoginResponseDTO;
import com.supbo.restaurant.dto.response.UserRegisterResponseDTO;

public interface AuthenticationService {
    UserLoginResponseDTO login(UserLoginRequestDTO loginRequest);

    void  register(UserRegisterRequestDTO registerRequest);
    void confirmRegistration(String email, String otp);


}
