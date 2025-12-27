package com.siupo.restaurant.repository;

import com.siupo.restaurant.enums.EProductStatus;
import com.siupo.restaurant.model.Combo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComboRepository extends JpaRepository<Combo, Long> {
    
    List<Combo> findByStatus(EProductStatus status);

    List<Combo> findByIdIn(List<Long> ids);
    
    @Query("SELECT c FROM Combo c LEFT JOIN FETCH c.items WHERE c.id = :id")
    Combo findByIdWithItems(Long id);
}
