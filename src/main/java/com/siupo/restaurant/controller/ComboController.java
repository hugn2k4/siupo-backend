package com.siupo.restaurant.controller;

import com.siupo.restaurant.dto.request.CreateComboRequest;
import com.siupo.restaurant.dto.response.ApiResponse;
import com.siupo.restaurant.dto.response.ComboResponse;
import com.siupo.restaurant.mapper.ComboMapper;
import com.siupo.restaurant.service.combo.ComboService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/combos")
@RequiredArgsConstructor
public class ComboController {

    private final ComboService comboService;
    private final ComboMapper comboMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<ComboResponse>> createCombo(@Valid @RequestBody CreateComboRequest request) {
        ComboResponse comboResponse = comboMapper.toResponse(comboService.createCombo(request));
        ApiResponse<ComboResponse> response = ApiResponse.<ComboResponse>builder()
                .success(true)
                .code("201")
                .message("Combo created successfully")
                .data(comboResponse)
                .build();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComboResponse>> getComboById(@PathVariable Long id) {
        ComboResponse comboResponse = comboMapper.toResponse(comboService.getComboById(id));
        ApiResponse<ComboResponse> response = ApiResponse.<ComboResponse>builder()
                .success(true)
                .code("200")
                .message("Combo retrieved successfully")
                .data(comboResponse)
                .build();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ComboResponse>>> getAllCombos(
            @RequestParam(required = false, defaultValue = "false") boolean availableOnly) {
        List<ComboResponse> combos = availableOnly 
                ? comboService.getAvailableCombos() 
                : comboService.getAllCombos();
        ApiResponse<List<ComboResponse>> response = ApiResponse.<List<ComboResponse>>builder()
                .success(true)
                .code("200")
                .message("Combos retrieved successfully")
                .data(combos)
                .build();
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<ComboResponse>> updateCombo(
            @PathVariable Long id, 
            @Valid @RequestBody CreateComboRequest request) {
        ComboResponse comboResponse = comboMapper.toResponse(comboService.updateCombo(id, request));
        ApiResponse<ComboResponse> response = ApiResponse.<ComboResponse>builder()
                .success(true)
                .code("202")
                .message("Combo updated successfully")
                .data(comboResponse)
                .build();
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCombo(@PathVariable Long id) {
        comboService.deleteCombo(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .code("204")
                .message("Combo deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<ComboResponse>> toggleComboStatus(@PathVariable Long id) {
        ComboResponse comboResponse = comboMapper.toResponse(comboService.toggleComboStatus(id));
        ApiResponse<ComboResponse> response = ApiResponse.<ComboResponse>builder()
                .success(true)
                .code("200")
                .message("Combo status updated successfully")
                .data(comboResponse)
                .build();
        return ResponseEntity.ok(response);
    }
}
