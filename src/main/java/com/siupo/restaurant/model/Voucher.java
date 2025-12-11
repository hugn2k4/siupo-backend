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
    private String code; // Mã voucher (VD: SAVE20, FREESHIP)

    @Column(nullable = false, length = 200)
    private String name; // Tên voucher

    @Column(length = 1000)
    private String description; // Mô tả chi tiết

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EVoucherType type; // PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING

    @Column(nullable = false)
    private Double discountValue; // Giá trị giảm (%, số tiền cố định)

    private Double minOrderValue; // Giá trị đơn hàng tối thiểu

    private Double maxDiscountAmount; // Số tiền giảm tối đa (cho % discount)

    @Column(nullable = false)
    @Builder.Default
    private Integer usageLimit = 0; // Số lần sử dụng tối đa (0 = unlimited)

    @Column(nullable = false)
    @Builder.Default
    private Integer usedCount = 0; // Số lần đã sử dụng

    private Integer usageLimitPerUser; // Giới hạn mỗi user (null = không giới hạn)

    @Column(nullable = false)
    private LocalDateTime startDate; // Ngày bắt đầu

    @Column(nullable = false)
    private LocalDateTime endDate; // Ngày hết hạn

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EVoucherStatus status = EVoucherStatus.ACTIVE; // ACTIVE, INACTIVE, EXPIRED

    @Builder.Default
    private Boolean isPublic = true; // true = hiển thị công khai, false = chỉ dùng với mã

    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoucherUsage> usages; // Lịch sử sử dụng

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
