package com.siupo.restaurant.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "pending_reg", timeToLive = 600)
@Data
@AllArgsConstructor
public class PendingRegistrationRedis {
    @Id
    private String email;
    private String fullName;
    private String password;
}