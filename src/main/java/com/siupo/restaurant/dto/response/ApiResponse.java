package com.siupo.restaurant.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private boolean success;

    @Builder.Default
    private String code = "";

    @Builder.Default
    private String message = "";

    @Builder.Default
    private T data = null;

    @Builder.Default
    private Instant timestamp = Instant.now();
}