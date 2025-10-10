package com.siupo.restaurant.dto;


import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {
    private String addressLine;
    private String ward;
    private String district;
    private String province;
    private String receiverName;
    private String receiverPhone;
}
