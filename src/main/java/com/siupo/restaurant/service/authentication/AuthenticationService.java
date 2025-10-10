package com.siupo.restaurant.service.authentication;

import com.siupo.restaurant.dto.request.LoginRequest;
import com.siupo.restaurant.dto.request.RegisterRequest;
import com.siupo.restaurant.dto.request.RefreshTokenRequest;
import com.siupo.restaurant.dto.request.LogoutRequest;
import com.siupo.restaurant.dto.response.AuthResponse;

public interface AuthenticationService {
    AuthResponse login(LoginRequest loginRequest);

    void  register(RegisterRequest registerRequest);
    void confirmRegistration(String email, String otp);

    void resendOtp(String email);
    
    AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
    
    void logout(LogoutRequest logoutRequest);
}
