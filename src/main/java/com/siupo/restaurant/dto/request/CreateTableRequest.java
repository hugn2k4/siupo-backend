package com.siupo.restaurant.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTableRequest {
    
    @NotBlank(message = "Số bàn không được để trống")
    private String tableNumber;
    
    @NotNull(message = "Số chỗ ngồi không được để trống")
    @Min(value = 1, message = "Số chỗ ngồi phải lớn hơn 0")
    private Integer seat;
}
