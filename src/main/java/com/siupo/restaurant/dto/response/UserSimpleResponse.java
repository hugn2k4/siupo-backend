package com.siupo.restaurant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSimpleResponse {
    private Long id;
    private String username;
    private String email;
    private String phoneNumber;
    private String fullname;
}