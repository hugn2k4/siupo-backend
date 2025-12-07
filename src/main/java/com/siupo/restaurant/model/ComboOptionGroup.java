package com.siupo.restaurant.model;

import com.siupo.restaurant.enums.EProductStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "combo_option_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComboOptionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combo_id", nullable = false)
    private Combo combo;

    @Column(nullable = false)
    private String name; // VD: "Chọn món phụ", "Chọn nước uống"

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Integer minSelection = 1; // Tối thiểu phải chọn bao nhiêu món

    @Column(nullable = false)
    @Builder.Default
    private Integer maxSelection = 1; // Tối đa được chọn bao nhiêu món

    @Column(nullable = false)
    @Builder.Default
    private Integer displayOrder = 0; // Thứ tự hiển thị

    @OneToMany(mappedBy = "comboOptionGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComboOptionItem> items;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EProductStatus status = EProductStatus.AVAILABLE;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
