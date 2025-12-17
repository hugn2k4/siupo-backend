package com.siupo.restaurant.service.token;

import com.siupo.restaurant.dto.response.LoginResponse;
import com.siupo.restaurant.model.User;

public interface TokenService {
    LoginResponse generateAuthResponse(User user);
    LoginResponse refreshAccessToken(String refreshToken);
    void revokeToken(String refreshToken);
}
