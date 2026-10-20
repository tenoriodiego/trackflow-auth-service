
package br.com.trackflow.auth.authentication.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.trackflow.auth.authentication.entity.RefreshToken;
import br.com.trackflow.auth.authentication.exception.InvalidRefreshTokenException;
import br.com.trackflow.auth.authentication.repository.RefreshTokenRepository;
import br.com.trackflow.auth.user.entity.User;
import br.com.trackflow.auth.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(
                refreshTokenRepository,
                userRepository,
                604800000L);

        user = new User();
        user.setId(1L);
        user.setEmail("diego@trackflow.com");
    }

    @Test
    void shouldCreateRefreshTokenAndStoreItsHash() {
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String rawToken = refreshTokenService.createRefreshToken(user);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken savedToken = captor.getValue();

        assertThat(rawToken).isNotBlank();
        assertThat(savedToken.getUser()).isEqualTo(user);
        assertThat(savedToken.getTokenHash()).hasSize(64);
        assertThat(savedToken.getTokenHash()).isEqualTo(hashToken(rawToken));
        assertThat(savedToken.getTokenHash()).isNotEqualTo(rawToken);
        assertThat(savedToken.getCreatedAt()).isNotNull();
        assertThat(savedToken.getExpiresAt())
                .isEqualTo(savedToken.getCreatedAt().plusDays(7));
    }

    @Test
    void shouldRotateValidRefreshToken() {
        String rawToken = "valid-refresh-token";
        String currentHash = hashToken(rawToken);

        LocalDateTime now = LocalDateTime.now();

        RefreshToken currentToken = new RefreshToken();
        currentToken.setUser(user);
        currentToken.setTokenHash(currentHash);
        currentToken.setCreatedAt(now.minusDays(1));
        currentToken.setExpiresAt(now.plusDays(1));

        when(refreshTokenRepository.findByTokenHash(currentHash))
                .thenReturn(Optional.of(currentToken));

        when(refreshTokenRepository.revokeIfActive(
                eq(currentHash), any(String.class), any(LocalDateTime.class)))
                .thenReturn(1);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RefreshTokenService.RotationResult result = refreshTokenService.rotateRefreshToken(rawToken);

        assertThat(result.user()).isEqualTo(user);
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotEqualTo(rawToken);

        verify(refreshTokenRepository).revokeIfActive(
                eq(currentHash), any(String.class), any(LocalDateTime.class));

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken replacement = captor.getValue();

        assertThat(replacement.getUser()).isEqualTo(user);
        assertThat(replacement.getTokenHash())
                .isEqualTo(hashToken(result.refreshToken()));
        assertThat(replacement.getTokenHash()).hasSize(64);
        assertThat(replacement.getExpiresAt())
                .isEqualTo(replacement.getCreatedAt().plusDays(7));
    }

    @Test
    void shouldRejectBlankRefreshToken() {
        assertThatThrownBy(
                () -> refreshTokenService.rotateRefreshToken(" "))
                .isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenRepository, never())
                .findByTokenHash(any(String.class));
    }

    @Test
    void shouldRejectRefreshTokenThatDoesNotExist() {
        String rawToken = "unknown-refresh-token";

        when(refreshTokenRepository.findByTokenHash(hashToken(rawToken)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> refreshTokenService.rotateRefreshToken(rawToken))
                .isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenRepository, never())
                .revokeIfActive(any(String.class), any(String.class),
                        any(LocalDateTime.class));
    }

    @Test
    void shouldRejectExpiredRefreshToken() {
        String rawToken = "expired-refresh-token";

        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setUser(user);
        expiredToken.setTokenHash(hashToken(rawToken));
        expiredToken.setCreatedAt(LocalDateTime.now().minusDays(8));
        expiredToken.setExpiresAt(LocalDateTime.now().minusSeconds(1));

        when(refreshTokenRepository.findByTokenHash(hashToken(rawToken)))
                .thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(
                () -> refreshTokenService.rotateRefreshToken(rawToken))
                .isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenRepository, never())
                .revokeIfActive(any(String.class), any(String.class),
                        any(LocalDateTime.class));
    }

    @Test
    void shouldRejectAlreadyRevokedRefreshToken() {
        String rawToken = "revoked-refresh-token";

        RefreshToken revokedToken = new RefreshToken();
        revokedToken.setUser(user);
        revokedToken.setTokenHash(hashToken(rawToken));
        revokedToken.setCreatedAt(LocalDateTime.now().minusDays(1));
        revokedToken.setExpiresAt(LocalDateTime.now().plusDays(1));
        revokedToken.setRevokedAt(LocalDateTime.now().minusMinutes(5));

        when(refreshTokenRepository.findByTokenHash(hashToken(rawToken)))
                .thenReturn(Optional.of(revokedToken));

        assertThatThrownBy(
                () -> refreshTokenService.rotateRefreshToken(rawToken))
                .isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenRepository, never())
                .revokeIfActive(any(String.class), any(String.class),
                        any(LocalDateTime.class));
    }

    @Test
    void shouldRejectRefreshTokenWhenAtomicRevocationFails() {
        String rawToken = "concurrent-refresh-token";
        String currentHash = hashToken(rawToken);

        RefreshToken currentToken = new RefreshToken();
        currentToken.setUser(user);
        currentToken.setTokenHash(currentHash);
        currentToken.setCreatedAt(LocalDateTime.now().minusDays(1));
        currentToken.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(refreshTokenRepository.findByTokenHash(currentHash))
                .thenReturn(Optional.of(currentToken));

        when(refreshTokenRepository.revokeIfActive(
                eq(currentHash), any(String.class), any(LocalDateTime.class)))
                .thenReturn(0);

        assertThatThrownBy(
                () -> refreshTokenService.rotateRefreshToken(rawToken))
                .isInstanceOf(InvalidRefreshTokenException.class);

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));

        verify(userRepository, never()).findById(any(Long.class));
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
