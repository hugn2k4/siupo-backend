package com.siupo.restaurant.service.authentication;

import com.siupo.restaurant.dto.request.*;
import com.siupo.restaurant.dto.response.LoginDataResponse;
import com.siupo.restaurant.dto.response.MessageDataReponse;
import com.siupo.restaurant.model.User;

public interface AuthenticationService {
    LoginDataResponse login(LoginRequest loginRequest);
    MessageDataReponse register(RegisterRequest registerRequest);
    MessageDataReponse confirmRegistration(String email, String otp);
    MessageDataReponse requestForgotPassword(String email);
    MessageDataReponse setNewPassword(ForgotPasswordRequest forgotPasswordRequest);
    void resendOtp(String email);
    LoginDataResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
    void logout(LogoutRequest logoutRequest);
    
    // OAuth2 methods
    User processOAuth2User(String email, String name, String picture);
}
