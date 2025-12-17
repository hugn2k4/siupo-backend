package com.siupo.restaurant.repository;

import com.siupo.restaurant.model.Address;
import com.siupo.restaurant.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address,Long> {
    Optional<Address> findByIdAndCustomer(Long id, Customer customer);
}
