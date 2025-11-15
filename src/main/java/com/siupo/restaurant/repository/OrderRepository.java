package com.siupo.restaurant.repository;

import com.siupo.restaurant.enums.EOrderStatus;
import com.siupo.restaurant.model.Order;
import com.siupo.restaurant.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    
    Page<Order> findByStatus(EOrderStatus status, Pageable pageable);
    
    Page<Order> findAll(Pageable pageable);
}
