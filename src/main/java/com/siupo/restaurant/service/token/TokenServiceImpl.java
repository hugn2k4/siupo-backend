package com.siupo.restaurant.service.token;

import com.siupo.restaurant.dto.response.LoginResponse;
import com.siupo.restaurant.exception.base.ErrorCode;
import com.siupo.restaurant.exception.business.UnauthorizedException;
import com.siupo.restaurant.model.RefreshToken;
import com.siupo.restaurant.model.User;
import com.siupo.restaurant.repository.RefreshTokenRepository;
import com.siupo.restaurant.security.jwt.JwtTokenProvider;
import jakarta.persistence.DiscriminatorValue;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TokenServiceImpl implements TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpiration;

    @Override
    @Transactional
    public LoginResponse generateAuthResponse(User user) {
        // 1. Tạo Access Token
        String role = getUserRole(user);
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), List.of("ROLE_" + role));
        // 2. Tạo Refresh Token
        String refreshTokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .revoked(false)
                .build();
        // 3. Revoke các token cũ (Optional: Nếu muốn single session)
        revokeAllUserTokens(user);
        refreshTokenRepository.save(refreshToken);
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .build();
    }

    @Override
    @Transactional
    public LoginResponse refreshAccessToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.INVALID_REFRESH_TOKEN));
        if (refreshToken.isRevoked() || refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new UnauthorizedException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        // Token Rotation: Hủy cái cũ, tạo cặp mới
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        return generateAuthResponse(refreshToken.getUser());
    }

    @Override
    public void revokeToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElse(null);
        // Nếu token không tồn tại hoặc đã revoked rồi thì bỏ qua (idempotent)
        if (refreshToken == null || refreshToken.isRevoked()) {
            return;
        }
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    private void revokeAllUserTokens(User user) {
        List<RefreshToken> validTokens = refreshTokenRepository.findAllByUserAndRevokedFalse(user);
        if (!validTokens.isEmpty()) {
            validTokens.forEach(t -> t.setRevoked(true));
            refreshTokenRepository.saveAll(validTokens);
        }
    }

    private String getUserRole(User user) {
        DiscriminatorValue val = user.getClass().getAnnotation(DiscriminatorValue.class);
        return val != null ? val.value() : user.getClass().getSimpleName().toUpperCase();
    }
}