package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.AddressDTO;
import com.siupo.restaurant.dto.request.ChangePasswordRequest;
import com.siupo.restaurant.dto.request.UserRequest;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.UserResponse;
import com.siupo.restaurant.model.User;
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
        //log.info("Endpoint /api/users/customer called for user: {}", user != null ? user.getEmail() : "null");
        User currentUser = userService.getCurrentUserInfo(user);
        UserResponse userResponse = mapToUserResponse(currentUser);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .code("200")
                .message("User information retrieved successfully")
                .data(userResponse)
                .build());
    }

    @PutMapping("/customer")
    public ResponseEntity<ApiResponse<UserResponse>> updateCurrentUser(@AuthenticationPrincipal User user,@Valid @RequestBody UserRequest request) {
        User updatedUser = userService.updateUserInfo(user, request);
        UserResponse userResponse = mapToUserResponse(updatedUser);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .code("200")
                .message("User information updated successfully")
                .data(userResponse)
                .build());
    }

    @GetMapping("/customer/addresses")
    public ResponseEntity<ApiResponse<List<AddressDTO>>> getUserAddresses(@AuthenticationPrincipal User user) {
        List<AddressDTO> addresses = userService.getUserAddresses(user);
        return ResponseEntity.ok(ApiResponse.<List<AddressDTO>>builder()
                .success(true)
                .code("200")
                .message("User addresses retrieved successfully")
                .data(addresses)
                .build());
    }

    @PostMapping("/customer/addresses")
    public ResponseEntity<ApiResponse<AddressDTO>> addAddress(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddressDTO addressDTO) {
        AddressDTO saved = userService.addAddress(user, addressDTO);
        return ResponseEntity.ok(ApiResponse.<AddressDTO>builder()
                .success(true)
                .code("201")
                .message("Address added successfully")
                .data(saved)
                .build());
    }

    @PutMapping("/customer/addresses/{id}")
    public ResponseEntity<ApiResponse<AddressDTO>> updateAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody AddressDTO addressDTO) {
        AddressDTO updated = userService.updateAddress(user, id, addressDTO);
        return ResponseEntity.ok(ApiResponse.<AddressDTO>builder()
                .success(true)
                .code("200")
                .message("Address updated successfully")
                .data(updated)
                .build());
    }

    @DeleteMapping("/customer/addresses/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        userService.deleteAddress(user, id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .code("200")
                .message("Address deleted successfully")
                .build());
    }
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole() != null ? user.getRole() : "User") // Lấy role từ User, mặc định là "User"
                .build();
    }

    @PutMapping("/customer/changepassword")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.changePassword(user, request);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .code("200")
                .message("Thay đổi mật khẩu thành công")
                .build());
    }
}
