package com.siupo.restaurant.service.authentication;

import com.siupo.restaurant.dto.request.UserLoginRequestDTO;
import com.siupo.restaurant.dto.request.UserRegisterRequestDTO;
import com.siupo.restaurant.dto.response.UserLoginResponseDTO;
import com.siupo.restaurant.dto.response.UserRegisterResponseDTO;

public interface AuthenticationService {
    UserLoginResponseDTO login(UserLoginRequestDTO loginRequest);

    void  register(UserRegisterRequestDTO registerRequest);
    void confirmRegistration(String email, String otp);


}
