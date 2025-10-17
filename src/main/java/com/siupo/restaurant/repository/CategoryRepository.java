package com.siupo.restaurant.repository;
import com.siupo.restaurant.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CategoryRepository extends JpaRepository<Category, Long>   {
}
