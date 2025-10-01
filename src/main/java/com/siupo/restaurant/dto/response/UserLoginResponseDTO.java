package com.siupo.restaurant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserLoginResponseDTO {
    private String token;
    private Long userId;
    private String username;
    private String email;
}