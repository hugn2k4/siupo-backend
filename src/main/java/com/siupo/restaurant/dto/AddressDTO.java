package com.siupo.restaurant.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class AddressDTO {
    private Long id;
    private String address;
    private String ward;
    private String district;
    private String province;
    private String receiverName;
    private String receiverPhone;
    private Boolean isDefault;
}