package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.request.ChangePasswordRequest;
import com.siupo.restaurant.dto.request.UpdateCustomerStatusRequest;
import com.siupo.restaurant.dto.request.UserRequest;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.UserResponse;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.siupo.restaurant.dto.ImageDTO;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

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
        ImageDTO avatarDTO = null;
        if (user.getAvatar() != null) {
            avatarDTO = ImageDTO.builder()
                    .id(user.getAvatar().getId())
                    .url(user.getAvatar().getUrl())
                    .name(user.getAvatar().getName())
                    .build();
        }
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role("CUSTOMER")
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .status(user.getStatus())
                .avatar(avatarDTO)
                .build();
    }

    @GetMapping("/customers")
    @PreAuthorize("hasRole('ADMIN')") // Chỉ admin được xem
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllCustomers(@AuthenticationPrincipal User currentUser) {
        List<User> customers = userService.getAllCustomers();
        List<UserResponse> responses = customers.stream()
                .map(this::mapToUserResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.<List<UserResponse>>builder()
                .success(true)
                .code("200")
                .message("Lấy danh sách khách hàng thành công")
                .data(responses)
                .build());
    }
    @PutMapping("/customers/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateCustomerStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerStatusRequest request) {

        userService.updateCustomerStatus(id, request.getStatus());

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .code("200")
                .message("Cập nhật trạng thái khách hàng thành công")
                .build());
    }
}
