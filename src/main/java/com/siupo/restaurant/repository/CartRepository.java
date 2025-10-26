package com.siupo.restaurant.repository;

import com.siupo.restaurant.model.Cart;
import com.siupo.restaurant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}
