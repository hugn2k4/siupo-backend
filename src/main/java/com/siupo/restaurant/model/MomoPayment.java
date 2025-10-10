package com.siupo.restaurant.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "momo_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class  MomoPayment extends Payment {
    private Long requestId;
    private Long transactionId;
    private Integer resultCode;
}
