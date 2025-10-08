package com.siupo.restaurant.service.authentication;

import com.siupo.restaurant.dto.request.LoginRequestDTO;
import com.siupo.restaurant.dto.request.RegisterRequestDTO;
import com.siupo.restaurant.dto.request.RefreshTokenRequestDTO;
import com.siupo.restaurant.dto.request.LogoutRequestDTO;
import com.siupo.restaurant.dto.response.AuthResponseDTO;

public interface AuthenticationService {
    AuthResponseDTO login(LoginRequestDTO loginRequest);

    void  register(RegisterRequestDTO registerRequest);
    void confirmRegistration(String email, String otp);

    void resendOtp(String email);
    
    AuthResponseDTO refreshToken(RefreshTokenRequestDTO refreshTokenRequest);
    
    void logout(LogoutRequestDTO logoutRequest);
}
