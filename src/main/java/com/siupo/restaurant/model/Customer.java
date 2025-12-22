package com.siupo.restaurant.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@DiscriminatorValue("CUSTOMER")
@ToString(exclude = {"orders", "cart", "wishlist", "addresses", "defaultAddress"})
public class Customer extends User {
    private Double totalSpent = 0.0;

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Cart cart;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Wishlist wishlist;
    
    @OneToMany(mappedBy = "customer", orphanRemoval = true)
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "default_address_id")
    private Address defaultAddress; 
}
