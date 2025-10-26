package com.siupo.restaurant.repository;

import com.siupo.restaurant.model.RefreshToken;
import com.siupo.restaurant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findAllByUserAndRevokedFalse(User user);
    boolean existsByToken (String token);
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user = :user")
    void revokeByUser(@Param("user") User user);
    /**
     * Đánh dấu revoked cho token theo token string
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.token = :token")
    void revokeByToken(@Param("token") String token);
    /**
     * Xóa các token đã expired
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") Instant now);
    /**
     * Xóa các token đã revoked
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revoked = true")
    void deleteRevokedTokens();
    /**
     * Kiểm tra token có tồn tại và active không
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token AND rt.revoked = false AND rt.expiryDate > :now")
    Optional<RefreshToken> findActiveByToken(@Param("token") String token, @Param("now") Instant now);
}