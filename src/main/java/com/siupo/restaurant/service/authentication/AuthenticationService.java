package com.siupo.restaurant.service.authentication;

import com.siupo.restaurant.dto.request.LoginRequest;
import com.siupo.restaurant.dto.request.RegisterRequest;
import com.siupo.restaurant.dto.request.RefreshTokenRequest;
import com.siupo.restaurant.dto.request.LogoutRequest;
import com.siupo.restaurant.dto.response.LoginDataResponse;
import com.siupo.restaurant.dto.response.MessageDataReponse;

public interface AuthenticationService {
    LoginDataResponse login(LoginRequest loginRequest);

    MessageDataReponse register(RegisterRequest registerRequest);
    MessageDataReponse confirmRegistration(String email, String otp);

    void resendOtp(String email);
    
    LoginDataResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
    
    void logout(LogoutRequest logoutRequest);
}
