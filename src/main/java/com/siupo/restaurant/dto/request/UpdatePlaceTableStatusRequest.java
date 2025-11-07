package com.siupo.restaurant.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePlaceTableStatusRequest {
    @NotNull(message = "Trạng thái không được để trống")
    private String status;
    private String note;
}