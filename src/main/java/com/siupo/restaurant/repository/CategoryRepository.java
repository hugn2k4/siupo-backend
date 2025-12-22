package com.siupo.restaurant.repository;
import com.siupo.restaurant.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long>   {
    long countByIdIn(List<Long> ids);
}
