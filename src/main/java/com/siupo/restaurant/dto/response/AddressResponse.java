package com.siupo.restaurant.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressResponse {
    private Long id;
    private String address;
    private String ward;
    private String district;
    private String province;
    private String receiverName;
    private String receiverPhone;
    private Boolean isDefault;
}