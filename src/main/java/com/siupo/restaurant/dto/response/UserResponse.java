package com.siupo.restaurant.dto.response;


import com.siupo.restaurant.enums.EGender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
    private LocalDate dateOfBirth;
    private EGender gender;
}
