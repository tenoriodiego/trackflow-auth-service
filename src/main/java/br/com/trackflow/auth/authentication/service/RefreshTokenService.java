
package br.com.trackflow.auth.authentication.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.trackflow.auth.authentication.entity.RefreshToken;
import br.com.trackflow.auth.authentication.exception.InvalidRefreshTokenException;
import br.com.trackflow.auth.authentication.repository.RefreshTokenRepository;
import br.com.trackflow.auth.user.entity.User;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenExpiration;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            @Value("${jwt.refresh-token-expiration:604800000}") long refreshTokenExpiration) {

        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    @Transactional
    public String createRefreshToken(User user) {
        String rawToken = generateToken();
        LocalDateTime now = LocalDateTime.now();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashToken(rawToken));
        refreshToken.setCreatedAt(now);
        refreshToken.setExpiresAt(
                now.plusNanos(refreshTokenExpiration * 1_000_000L));

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional
    public RotationResult rotateRefreshToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidRefreshTokenException();
        }

        LocalDateTime now = LocalDateTime.now();
        String currentHash = hashToken(rawToken);

        RefreshToken current = refreshTokenRepository
                .findByTokenHash(currentHash)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (current.getRevokedAt() != null
                || !current.getExpiresAt().isAfter(now)) {
            throw new InvalidRefreshTokenException();
        }

        String replacementToken = generateToken();
        String replacementHash = hashToken(replacementToken);

        int revoked = refreshTokenRepository.revokeIfActive(
                currentHash,
                replacementHash,
                now);

        if (revoked != 1) {
            throw new InvalidRefreshTokenException();
        }

        RefreshToken replacement = new RefreshToken();
        replacement.setUser(current.getUser());
        replacement.setTokenHash(replacementHash);
        replacement.setCreatedAt(now);
        replacement.setExpiresAt(
                now.plusNanos(refreshTokenExpiration * 1_000_000L));

        refreshTokenRepository.save(replacement);

        return new RotationResult(current.getUser(), replacementToken);
    }

    private String generateToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "Não foi possível calcular o hash do token.",
                    exception);
        }
    }

    public record RotationResult(User user, String refreshToken) {
    }
}
