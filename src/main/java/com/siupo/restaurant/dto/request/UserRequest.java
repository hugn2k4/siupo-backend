package com.siupo.restaurant.dto.request;

import com.siupo.restaurant.enums.EGender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    @NotBlank(message = "Full name cannot be blank")
    private String avatarUrl;
    private String avatarName;
    private String fullName;
    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be 10 digits")
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private EGender gender;
}
