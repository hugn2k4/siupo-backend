package com.siupo.restaurant.mapper;

import com.siupo.restaurant.dto.ImageDTO;
import com.siupo.restaurant.dto.UserDTO;
import com.siupo.restaurant.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toDto(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .role(getUserRole(user))
                .build();

        if (user.getAvatar() != null) {
            dto.setAvatar(ImageDTO.builder()
                    .id(user.getAvatar().getId())
                    .url(user.getAvatar().getUrl())
                    .build());
        }
        return dto;
    }

    private String getUserRole(User user) {
        return user.getClass().getSimpleName().toUpperCase();
    }

}
