package com.siupo.restaurant.repository;

import com.siupo.restaurant.model.PendingRegistrationRedis;
import org.springframework.data.repository.CrudRepository;

public interface PendingRegistrationRepository extends CrudRepository<PendingRegistrationRedis, String> {
}
