package br.com.trackflow.auth.authentication.service;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {

        jwtService = new JwtService(
                "trackflow-development-secret-key-2026-very-secure",
                3600);
    }

    @Test
    void shouldGenerateAccessToken() {

        String token = jwtService.generateAccessToken(
                "diego@trackflow.com");

        assertThat(token)
                .isNotBlank();

        assertThat(jwtService.isTokenValid(token))
                .isTrue();
    }

    @Test
    void shouldExtractUsernameFromToken() {

        String token = jwtService.generateAccessToken(
                "diego@trackflow.com");

        String username = jwtService.extractUsername(token);

        assertThat(username)
                .isEqualTo("diego@trackflow.com");
    }

    @Test
    void shouldReturnFalseWhenTokenIsInvalid() {

        assertThat(
                jwtService.isTokenValid("token-invalido")).isFalse();
    }

    @Test
    void shouldReturnFalseWhenTokenWasSignedWithAnotherKey() {

        JwtService anotherJwtService = new JwtService(
                "another-trackflow-secret-key-2026-very-secure",
                3600);

        String token = anotherJwtService.generateAccessToken(
                "diego@trackflow.com");

        assertThat(jwtService.isTokenValid(token))
                .isFalse();
    }
}