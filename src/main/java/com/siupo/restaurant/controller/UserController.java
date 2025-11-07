package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.AddressDTO;
import com.siupo.restaurant.dto.request.AddressUpdateRequest;
import com.siupo.restaurant.dto.request.ChangePasswordRequest;
import com.siupo.restaurant.dto.request.UserRequest;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.UserResponse;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.service.address.AddressService;
import com.siupo.restaurant.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/customer")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal User user) {
        User currentUser = userService.getCurrentUserInfo(user);
        UserResponse userResponse = mapToUserResponse(currentUser);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true).code("200").message("User information retrieved successfully")
                .data(userResponse).build());
    }

    @PutMapping("/customer")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserRequest request) {
        User updatedUser = userService.updateUserInfo(user, request);
        UserResponse userResponse = mapToUserResponse(updatedUser);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true).code("200").message("User information updated successfully")
                .data(userResponse).build());
    }

    @PutMapping("/customer/changepassword")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(user, request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).code("200").message("Thay đổi mật khẩu thành công").build());
    }

    // ==================== HELPER ====================

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role("CUSTOMER")
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .build();
    }
}
