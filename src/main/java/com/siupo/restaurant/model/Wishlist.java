package com.siupo.restaurant.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wishlists")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "wishlist", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WishlistItem> items = new ArrayList<>();
    
    // Helper methods to add/remove products
    public void addProduct(Product product) {
        WishlistItem item = WishlistItem.builder()
                .wishlist(this)
                .product(product)
                .build();
        items.add(item);
    }
    
    public void removeProduct(Product product) {
        items.removeIf(item -> item.getProduct().equals(product));
    }
}
