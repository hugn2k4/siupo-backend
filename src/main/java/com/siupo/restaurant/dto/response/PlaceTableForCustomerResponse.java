package com.siupo.restaurant.dto.response;

import com.siupo.restaurant.enums.EPlaceTableStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceTableForCustomerResponse {

    private Long id;
    private Integer member;
    private EPlaceTableStatus status;
    private String phoneNumber;
    private Double totalPrice;
    private LocalDateTime startedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String note;
    private UserSimpleResponse user;
    private List<OrderItemResponse> items;
    private PaymentResponse payment;
    private Boolean hasPreOrder; // Có chọn món trước hay không
}