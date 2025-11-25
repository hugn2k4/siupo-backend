package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.AddressDTO;
import com.siupo.restaurant.dto.request.AddressUpdateRequest;
import com.siupo.restaurant.dto.response.AddressResponse;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.MessageDataReponse;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.service.address.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/address")
public class AddressController {
    private final AddressService addressService;
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
        public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddressesByUser(@AuthenticationPrincipal User user) {
        List<AddressResponse> addresses = addressService.getAddresses(user);
        return ResponseEntity.ok(ApiResponse.<List<AddressResponse>>builder()
                .success(true).code("200").message("User addresses retrieved successfully")
                .data(addresses).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddressDTO addressDTO) {
        AddressResponse addressResponse = addressService.addAddress(user, addressDTO);
        ApiResponse<AddressResponse> response = ApiResponse.<AddressResponse>builder()
                .success(true)
                .code("201")
                .message("Address added Successfully")
                .data(addressResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(@Valid @RequestBody AddressUpdateRequest request) {
        AddressResponse addressResponse = addressService.updateAddress(request.getAddressId(), request.getUpdateAddress());
        ApiResponse<AddressResponse> response = ApiResponse.<AddressResponse>builder()
                .success(true)
                .code("202")
                .message("Address updated Successfully")
                .data(addressResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal User user,
            @Valid @RequestParam Long addressId) {
        MessageDataReponse messageDataReponse = addressService.deleteAddress(user, addressId);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(messageDataReponse.isSuccess())
                .code(messageDataReponse.getCode())
                .message(messageDataReponse.getMessage())
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/default")
    public ResponseEntity<ApiResponse<AddressResponse>> getDefaultAddress(@AuthenticationPrincipal User user) {
        AddressResponse addressResponse = addressService.getAddressDefault(user);
        ApiResponse<AddressResponse> response = ApiResponse.<AddressResponse>builder()
                .success(true)
                .code("200")
                .message("Lấy địa chỉ mặc định thành công")
                .data(addressResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/default/{id}")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefaultAddress(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Long addressId
    ) {
        AddressResponse result = addressService.setAddressDefault(user, addressId);

        ApiResponse<AddressResponse> response = ApiResponse.<AddressResponse>builder()
                .success(true)
                .code("200")
                .message("Set default address successfully")
                .data(result)
                .build();

        return ResponseEntity.ok(response);
    }
}
