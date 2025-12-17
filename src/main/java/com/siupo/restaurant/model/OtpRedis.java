package com.siupo.restaurant.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import java.io.Serializable;

@AllArgsConstructor
@Data
@RedisHash(value = "OtpVerification", timeToLive = 300)
public class OtpRedis implements Serializable {
    @Id
    private String email;
    private String otp;
}