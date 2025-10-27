package com.siupo.restaurant.repository;

import com.siupo.restaurant.model.OrderAtTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OrderAtTableRepository extends JpaRepository<OrderAtTable, Long> {

    @Query("SELECT o FROM OrderAtTable o JOIN FETCH o.items WHERE o.id = :id")
    Optional<OrderAtTable> findByIdWithItems(@Param("id") Long id);
}