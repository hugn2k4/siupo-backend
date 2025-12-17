package com.siupo.restaurant.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreOrderPaymentRequest {
    private Long amount;
    private String description;
    private List<Map<String, Object>> items;
    private Map<String, String> customerInfo;
}