package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.AddressDTO;
import com.siupo.restaurant.dto.request.ChangePasswordRequest;
import com.siupo.restaurant.dto.request.UserRequest;
import com.siupo.restaurant.dto.response.AddressResponse;
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
    private final AddressService addressService;
    public UserController(UserService userService,AddressService addressService) {
        this.userService = userService;
        this.addressService = addressService;
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

    // ==================== ADDRESS ====================

    @GetMapping("/customer/addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getUserAddresses(@AuthenticationPrincipal User user) {
        List<AddressResponse> addresses = addressService.getAddresses(user);
        return ResponseEntity.ok(ApiResponse.<List<AddressResponse>>builder()
                .success(true).code("200").message("User addresses retrieved successfully")
                .data(addresses).build());
    }
    @PostMapping("/customer/addresses")
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddressDTO addressDTO) {
        AddressResponse saved = addressService.addAddress(user, addressDTO);
        return ResponseEntity.ok(ApiResponse.<AddressResponse>builder()
                .success(true).code("201").message("Address added successfully")
                .data(saved).build());
    }

    @PutMapping("/customer/addresses/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody AddressDTO addressDTO) {
        AddressResponse updated = addressService.updateAddress(user, id, addressDTO);
        return ResponseEntity.ok(ApiResponse.<AddressResponse>builder()
                .success(true).code("200").message("Address updated successfully")
                .data(updated).build());
    }

    @DeleteMapping("/customer/addresses/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        addressService.deleteAddress(user, id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).code("200").message("Address deleted successfully").build());
    }

    @PatchMapping("/customer/addresses/{id}/default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        AddressResponse dto = addressService.setDefaultAddress(user, id);
        return ResponseEntity.ok(ApiResponse.<AddressResponse>builder()
                .success(true).code("200").message("Đặt làm địa chỉ mặc định")
                .data(dto).build());
    }

    @GetMapping("/customer/addresses/default")
    public ResponseEntity<ApiResponse<AddressResponse>> getDefaultAddress(@AuthenticationPrincipal User user) {
        AddressResponse dto = addressService.getDefaultAddress(user);
        return ResponseEntity.ok(ApiResponse.<AddressResponse>builder()
                .success(true).code("200").message("Lấy địa chỉ mặc định")
                .data(dto).build());
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
