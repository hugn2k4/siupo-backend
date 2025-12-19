package com.siupo.restaurant.repository;

import com.siupo.restaurant.model.ProductTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductTagRepository extends JpaRepository<ProductTag, Long> {
    Optional<ProductTag> findByName(String name);
    List<ProductTag> findAllByNameIn(List<String> names);
}
