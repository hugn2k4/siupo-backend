package com.siupo.restaurant.repository;

import com.siupo.restaurant.model.OrderItem;
import com.siupo.restaurant.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    Optional<Review> findByOrderItem(OrderItem orderItem);
    
    @Query("SELECT r FROM Review r WHERE r.orderItem.id = :orderItemId")
    Optional<Review> findByOrderItemId(@Param("orderItemId") Long orderItemId);
    
    @Query("SELECT r FROM Review r WHERE r.orderItem.order.id = :orderId")
    List<Review> findByOrderId(@Param("orderId") Long orderId);
    
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId ORDER BY r.createdAt DESC")
    List<Review> findByProductId(@Param("productId") Long productId);
    
    @Query("SELECT r FROM Review r WHERE r.product.id IN :productIds")
    List<Review> findByProductIdIn(@Param("productIds") List<Long> productIds);
}
