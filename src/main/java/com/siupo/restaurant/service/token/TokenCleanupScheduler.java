package com.siupo.restaurant.service.token;

import com.siupo.restaurant.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {
    private final RefreshTokenRepository refreshTokenRepository;
    /**
     * Xóa các refresh token đã hết hạn hoặc đã bị revoke.
     * Chạy mỗi ngày lúc 3 giờ sáng để giữ database sạch sẽ.
     */
    @Scheduled(cron = "0 0 3 * * *") // 3:00 AM every day
    @Transactional
    public void cleanupTokens() {
        log.info("Starting refresh token cleanup task...");
        try {
            Instant now = Instant.now();
            // Xóa các token đã expired
            refreshTokenRepository.deleteExpiredTokens(now);
            log.info("Deleted expired tokens (expiry date < {})", now);
            // Xóa các token đã revoked
            refreshTokenRepository.deleteRevokedTokens();
            log.info("Deleted revoked tokens");
            log.info("Refresh token cleanup completed successfully");
        } catch (Exception e) {
            log.error("Error during refresh token cleanup: {}", e.getMessage(), e);
        }
    }
    /**
     * Cleanup task cũng chạy mỗi 6 giờ một lần để đảm bảo token không tồn tại lâu.
     * Có thể disable cái này nếu chỉ muốn chạy 1 lần/ngày.
     */
    @Scheduled(fixedRate = 21600000) // Every 6 hours (6 * 60 * 60 * 1000 ms)
    @Transactional
    public void periodicCleanup() {
        log.debug("Running periodic token cleanup...");
        try {
            Instant now = Instant.now();
            refreshTokenRepository.deleteExpiredTokens(now);
            log.debug("Periodic token cleanup completed");
        } catch (Exception e) {
            log.error("Error during periodic token cleanup: {}", e.getMessage(), e);
        }
    }
}
