package com.siupo.restaurant.repository;

import com.siupo.restaurant.model.Cart;
import com.siupo.restaurant.model.CartItem;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart(Cart cart);

    @Transactional
    void deleteByCartAndProductIdIn(Cart cart, Collection<Long> productIds);
}
