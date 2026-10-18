
package br.com.trackflow.auth.authentication.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import br.com.trackflow.auth.authentication.dto.LoginRequest;
import br.com.trackflow.auth.authentication.dto.LoginResponse;
import br.com.trackflow.auth.user.entity.User;
import br.com.trackflow.auth.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

        @Mock
        private AuthenticationManager authenticationManager;

        @Mock
        private JwtService jwtService;

        @Mock
        private RefreshTokenService refreshTokenService;

        @Mock
        private UserRepository userRepository;

        private AuthenticationService authenticationService;

        @BeforeEach
        void setUp() {
                authenticationService = new AuthenticationService(
                                authenticationManager,
                                jwtService,
                                refreshTokenService,
                                userRepository);
        }

        @Test
        void shouldAuthenticateUserAndGenerateTokens() {
                LoginRequest request = new LoginRequest(
                                "diego@trackflow.com",
                                "12345678");

                User user = new User();
                user.setEmail("diego@trackflow.com");

                when(userRepository.findByEmailIgnoreCase("diego@trackflow.com"))
                                .thenReturn(Optional.of(user));

                when(jwtService.generateAccessToken("diego@trackflow.com"))
                                .thenReturn("jwt-token");

                when(refreshTokenService.createRefreshToken(user))
                                .thenReturn("refresh-token");

                LoginResponse response = authenticationService.login(request);

                assertThat(response.accessToken()).isEqualTo("jwt-token");
                assertThat(response.refreshToken()).isEqualTo("refresh-token");
                assertThat(response.tokenType()).isEqualTo("Bearer");

                verify(authenticationManager).authenticate(
                                any(UsernamePasswordAuthenticationToken.class));

                verify(jwtService).generateAccessToken("diego@trackflow.com");
                verify(refreshTokenService).createRefreshToken(user);
        }
}
