package com.siupo.restaurant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRegisterResponseDTO {
    private Long userId;
    private String username;
    private String fullName;
    private String phone;
    private String email;
    private String message;
}