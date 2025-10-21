package com.siupo.restaurant.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.siupo.restaurant.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginDataResponse {
    private String message;
    private String accessToken;
    @JsonIgnore
    private String refreshToken;
    private UserDTO user;
}