package com.siupo.restaurant.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.siupo.restaurant.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String accessToken;
    @JsonIgnore
    private String refreshToken;
    private UserDTO user;
}