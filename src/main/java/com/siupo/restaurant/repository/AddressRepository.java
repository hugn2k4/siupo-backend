package com.siupo.restaurant.repository;

import com.siupo.restaurant.model.Address;
import com.siupo.restaurant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address,Long> {
    List<Address> findByUser(User user);
    Optional<Address> findByIdAndUserId(Long id, Long userId);
    long countByUserId(Long userId);
}
