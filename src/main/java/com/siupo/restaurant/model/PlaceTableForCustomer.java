package com.siupo.restaurant.model;

import com.siupo.restaurant.enums.EPlaceTableStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "place_table_customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceTableForCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer member;
    @Enumerated(EnumType.STRING)
    private EPlaceTableStatus status;
    private String email;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "placeTable", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    private Double totalPrice = 0.0;

    private LocalDateTime startedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    private String note;
}
