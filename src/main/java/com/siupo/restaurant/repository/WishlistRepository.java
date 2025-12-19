package com.siupo.restaurant.repository;

import com.siupo.restaurant.model.Wishlist;
import com.siupo.restaurant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    @Query("""
        SELECT w FROM Wishlist w
        LEFT JOIN FETCH w.items i
        WHERE w.user = :user
    """)
    Optional<Wishlist> findByUser(@Param("user") User user);
}