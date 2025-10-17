package com.siupo.restaurant.service.authentication;

import com.siupo.restaurant.dto.request.*;
import com.siupo.restaurant.dto.response.LoginDataResponse;
import com.siupo.restaurant.dto.response.MessageDataReponse;

public interface AuthenticationService {
    LoginDataResponse login(LoginRequest loginRequest);
    MessageDataReponse register(RegisterRequest registerRequest);
    MessageDataReponse confirmRegistration(String email, String otp);
    MessageDataReponse requestForgotPassword(String email);
    MessageDataReponse setNewPassword(ForgotPasswordRequest forgotPasswordRequest);
    void resendOtp(String email);
    LoginDataResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
    void logout(LogoutRequest logoutRequest);
}
