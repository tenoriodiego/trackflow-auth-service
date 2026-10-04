package br.com.trackflow.auth.authentication.service;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import br.com.trackflow.auth.authentication.dto.LoginRequest;
import br.com.trackflow.auth.authentication.dto.LoginResponse;

class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);

        authenticationService = new AuthenticationService(
                authenticationManager,
                jwtService);
    }

    @Test
    void shouldAuthenticateUserAndGenerateAccessToken() {

        LoginRequest request = new LoginRequest(
                "diego@trackflow.com",
                "12345678");

        when(jwtService.generateAccessToken(
                "diego@trackflow.com")).thenReturn("jwt-token");

        LoginResponse response = authenticationService.login(request);

        assertThat(response.accessToken())
                .isEqualTo("jwt-token");

        assertThat(response.refreshToken())
                .isNull();

        assertThat(response.tokenType())
                .isEqualTo("Bearer");

        verify(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        verify(jwtService)
                .generateAccessToken(
                        "diego@trackflow.com");
    }
}