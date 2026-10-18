package br.com.trackflow.auth.authentication.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.trackflow.auth.authentication.dto.LoginResponse;
import br.com.trackflow.auth.authentication.dto.RefreshTokenRequest;
import br.com.trackflow.auth.authentication.entity.RefreshToken;
import br.com.trackflow.auth.authentication.exception.InvalidRefreshTokenException;
import br.com.trackflow.auth.authentication.repository.RefreshTokenRepository;
import br.com.trackflow.auth.user.entity.User;
import br.com.trackflow.auth.user.repository.UserRepository;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final Duration refreshTokenExpiration;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            @Value("${jwt.refresh-token-expiration:604800000}") long refreshTokenExpiration) {

        if (refreshTokenExpiration <= 0) {
            throw new IllegalArgumentException(
                    "A duração do refresh token deve ser positiva.");
        }

        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.refreshTokenExpiration = Duration.ofMillis(
                refreshTokenExpiration);
    }

    @Transactional
    public String createRefreshToken(User user) {
        LocalDateTime now = LocalDateTime.now();

        String rawToken = generateToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashToken(rawToken));
        refreshToken.setCreatedAt(now);
        refreshToken.setExpiresAt(
                now.plus(refreshTokenExpiration));

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

        // Capture o ID antes da atualização em lote do repositório.
        Long userId = current.getUser().getId();

        String replacementToken = generateToken();
        String replacementHash = hashToken(replacementToken);

        int revoked = refreshTokenRepository.revokeIfActive(
                currentHash,
                replacementHash,
                now);

        if (revoked != 1) {
            throw new InvalidRefreshTokenException();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(InvalidRefreshTokenException::new);

        RefreshToken replacement = new RefreshToken();
        replacement.setUser(user);
        replacement.setTokenHash(replacementHash);
        replacement.setCreatedAt(now);
        replacement.setExpiresAt(
                now.plus(refreshTokenExpiration));

        refreshTokenRepository.save(replacement);

        return new RotationResult(user, replacementToken);
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
                    "Não foi possível calcular o hash do refresh token.",
                    exception);
        }
    }

    public record RotationResult(User user, String refreshToken) {
    }

    public LoginResponse refresh(RefreshTokenRequest request) {
        RefreshTokenService.RotationResult result = refreshTokenService.rotateRefreshToken(
                request.refreshToken());

        String accessToken = jwtService.generateAccessToken(
                result.user().getEmail());

        return new LoginResponse(
                accessToken,
                result.refreshToken(),
                "Bearer");
    }
}