package com.siupo.restaurant.dto.request;

import com.siupo.restaurant.enums.EUserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCustomerStatusRequest {
    @NotNull(message = "Trạng thái không được để trống")
    private EUserStatus status;
}
