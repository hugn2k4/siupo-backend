package com.siupo.restaurant.repository;

import com.siupo.restaurant.model.Address;
import com.siupo.restaurant.model.Customer;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address,Long> {
    Optional<Address> findByIdAndCustomer(Long id, Customer customer);

    @Query("SELECT a FROM Address a WHERE a.customer = :customer")
    List<Address> findAllByCustomer(@Param("customer") Customer customer);
}
