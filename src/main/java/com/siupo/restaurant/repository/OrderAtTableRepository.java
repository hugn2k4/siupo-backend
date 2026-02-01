package com.siupo.restaurant.repository;

import com.siupo.restaurant.enums.EOrderStatus;
import com.siupo.restaurant.model.OrderAtTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OrderAtTableRepository extends JpaRepository<OrderAtTable, Long> {

    @Query("SELECT o FROM OrderAtTable o JOIN FETCH o.items WHERE o.id = :id")
    Optional<OrderAtTable> findByIdWithItems(@Param("id") Long id);

    Page<OrderAtTable> findByTableId(Long tableId, Pageable pageable);

    Page<OrderAtTable> findByStatus(EOrderStatus status, Pageable pageable);

    Page<OrderAtTable> findByTableIdAndStatus(Long tableId, EOrderStatus status, Pageable pageable);
}