package com.siupo.restaurant.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BannerRequest {

    @NotBlank(message = "Tên banner không được để trống")
    private String name;

    @NotBlank(message = "URL hình ảnh không được để trống")
    private String url;

    @NotNull(message = "Vị trí không được để trống")
    private String position;
}