package com.siupo.restaurant.service.authentication;

import com.siupo.restaurant.dto.request.*;
import com.siupo.restaurant.dto.response.LoginResponse;
import com.siupo.restaurant.model.User;

public interface AuthenticationService {
    LoginResponse login(LoginRequest loginRequest);
    void register(RegisterRequest registerRequest);
    void confirmRegistration(ConfirmRegistrationRequest confirmRegistrationRequest);
    void requestForgotPassword(String email);
    void setNewPassword(ConfirmForgotPasswordRequest confirmForgotPasswordRequest);
    void resendOtp(String email);
    LoginResponse refreshToken(String refreshToken);
    void logout(String refreshToken);
    User processOAuth2User(String email, String name, String picture);
}
