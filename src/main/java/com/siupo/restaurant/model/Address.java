package com.siupo.restaurant.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "addresses",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_address_full_content",
                columnNames = {
                        "user_id",
                        "address",
                        "ward",
                        "district",
                        "province",
                        "receiver_name",
                        "receiver_phone"
                }
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receiver_name", nullable = false, length = 100)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 11)
    private String receiverPhone;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "ward", nullable = false, length = 100)
    private String ward;

    @Column(name = "district", nullable = false, length = 100)
    private String district;

    @Column(name = "province", nullable = false, length = 100)
    private String province;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private Customer customer;
}
