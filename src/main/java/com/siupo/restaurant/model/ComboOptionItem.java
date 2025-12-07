package com.siupo.restaurant.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "combo_option_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComboOptionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combo_option_group_id", nullable = false)
    private ComboOptionGroup comboOptionGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    @Builder.Default
    private Double extraPrice = 0.0; // Giá phụ thu nếu chọn món này

    @Column(nullable = false)
    @Builder.Default
    private Integer displayOrder = 0; // Thứ tự hiển thị

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
