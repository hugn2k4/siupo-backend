package com.siupo.restaurant.repository;

import com.siupo.restaurant.model.OtpRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpRedisRepository extends CrudRepository<OtpRedis, String> {
}