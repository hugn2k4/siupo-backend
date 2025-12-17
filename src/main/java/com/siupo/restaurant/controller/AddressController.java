package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.request.AddressRequest;
import com.siupo.restaurant.dto.response.AddressResponse;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.service.address.AddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/address")
public class AddressController {
    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(@AuthenticationPrincipal User user) {
        List<AddressResponse> addressesResponse = addressService.getAddresses(user);
        ApiResponse<List<AddressResponse>> response = ApiResponse.<List<AddressResponse>>builder()
                .success(true)
                .code("200")
                .message("User addresses retrieved successfully")
                .data(addressesResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddressRequest address) {
        AddressResponse addressResponse = addressService.addAddress(user, address);
        ApiResponse<AddressResponse> response = ApiResponse.<AddressResponse>builder()
                .success(true)
                .code("201")
                .message("Address added Successfully")
                .data(addressResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {
        AddressResponse addressResponse = addressService.updateAddress(user,addressId, request);
        ApiResponse<AddressResponse> response = ApiResponse.<AddressResponse>builder()
                .success(true)
                .code("202")
                .message("Address updated Successfully")
                .data(addressResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal User user,
            @PathVariable Long addressId) {
        addressService.deleteAddress(user, addressId);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .code("200")
                .message("Address deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/default")
    public ResponseEntity<ApiResponse<AddressResponse>> getDefaultAddress(@AuthenticationPrincipal User user) {
        AddressResponse addressResponse = addressService.getAddressDefault(user);
        ApiResponse<AddressResponse> response = ApiResponse.<AddressResponse>builder()
                .success(true)
                .code("200")
                .message("Get default address successfully")
                .data(addressResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/default/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Long addressId) {
        AddressResponse addressResponse = addressService.setAddressDefault(user, addressId);
        ApiResponse<AddressResponse> response = ApiResponse.<AddressResponse>builder()
                .success(true)
                .code("200")
                .message("Set default address successfully")
                .data(addressResponse)
                .build();
        return ResponseEntity.ok(response);
    }
}
