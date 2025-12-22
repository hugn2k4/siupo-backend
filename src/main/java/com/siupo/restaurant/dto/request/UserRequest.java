package com.siupo.restaurant.dto.request;

import com.siupo.restaurant.enums.EGender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Pattern(
            regexp = "^(https?://).+",
            message = "Avatar URL must be a valid URL"
    )
    private String avatarUrl;

    @Size(max = 255, message = "Avatar name must not exceed 255 characters")
    private String avatarName;

    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @Pattern(
            regexp = "^\\d{10}$",
            message = "Phone number must be exactly 10 digits"
    )
    private String phoneNumber;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private EGender gender;
}
