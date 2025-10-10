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

    private Integer memberInt;

    @Enumerated(EnumType.STRING)
    private EPlaceTableStatus status;

    private String fullname;
    private String phoneNumber;

    @CreationTimestamp
    private LocalDateTime startedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
