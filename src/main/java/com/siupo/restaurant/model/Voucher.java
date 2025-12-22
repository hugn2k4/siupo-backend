package com.siupo.restaurant.model;

import com.siupo.restaurant.enums.EVoucherStatus;
import com.siupo.restaurant.enums.EVoucherType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "vouchers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EVoucherType type;

    @Column(nullable = false)
    private Double discountValue;

    private Double minOrderValue;

    private Double maxDiscountAmount;

    @Column(nullable = false)
    @Builder.Default
    private Integer usageLimit = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    private Integer usageLimitPerUser;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EVoucherStatus status = EVoucherStatus.ACTIVE;

    @Builder.Default
    private Boolean isPublic = true;

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoucherUsage> usages;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
