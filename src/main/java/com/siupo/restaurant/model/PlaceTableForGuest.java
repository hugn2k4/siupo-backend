package com.siupo.restaurant.model;

import com.siupo.restaurant.enums.EPlaceTableStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "place_table_guests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceTableForGuest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fullname;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Column(length = 100)
    private String email;

    @Column(nullable = false)
    private Integer memberInt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EPlaceTableStatus status;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(length = 500)
    private String note;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}