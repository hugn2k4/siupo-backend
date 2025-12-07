package com.siupo.restaurant.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateComboRequest {
    
    @NotBlank(message = "Tên combo không được để trống")
    private String name;
    
    private String description;
    
    @NotNull(message = "Giá combo không được để trống")
    @Min(value = 0, message = "Giá combo phải >= 0")
    private Double basePrice;
    
    private List<String> imageUrls;
    
    @NotNull(message = "Combo phải có ít nhất 1 sản phẩm")
    private List<ComboItemRequest> items;
}

