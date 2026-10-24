
package br.com.trackflow.auth.authentication.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import br.com.trackflow.auth.authentication.dto.LoginRequest;
import br.com.trackflow.auth.authentication.dto.LoginResponse;
import br.com.trackflow.auth.authentication.dto.RefreshTokenRequest;
import br.com.trackflow.auth.authentication.exception.InvalidRefreshTokenException;
import br.com.trackflow.auth.user.entity.User;
import br.com.trackflow.auth.user.repository.UserRepository;

import java.util.Set;
import java.util.NoSuchElementException;

import br.com.trackflow.auth.role.entity.Role;
import br.com.trackflow.auth.role.entity.RoleName;
import br.com.trackflow.auth.shared.exception.UserNotFoundException;
import br.com.trackflow.auth.user.dto.AuthenticatedUserResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
                verify(userRepository).findByEmailIgnoreCase("diego@trackflow.com");
                verify(jwtService).generateAccessToken("diego@trackflow.com");
                verify(refreshTokenService).createRefreshToken(user);
        }

        @Test
        void shouldNotGenerateTokensWhenCredentialsAreInvalid() {
                LoginRequest request = new LoginRequest(
                                "diego@trackflow.com",
                                "wrong-password");

                when(authenticationManager.authenticate(
                                any(UsernamePasswordAuthenticationToken.class)))
                                .thenThrow(new BadCredentialsException("Credenciais inválidas."));

                assertThatThrownBy(() -> authenticationService.login(request))
                                .isInstanceOf(BadCredentialsException.class);

                verifyNoInteractions(userRepository, jwtService, refreshTokenService);
        }

        @Test
        void shouldRefreshAccessAndRefreshTokens() {
                User user = new User();
                user.setEmail("diego@trackflow.com");

                RefreshTokenRequest request = new RefreshTokenRequest("old-refresh-token");

                RefreshTokenService.RotationResult rotationResult = new RefreshTokenService.RotationResult(
                                user,
                                "new-refresh-token");

                when(refreshTokenService.rotateRefreshToken("old-refresh-token"))
                                .thenReturn(rotationResult);

                when(jwtService.generateAccessToken("diego@trackflow.com"))
                                .thenReturn("new-access-token");

                LoginResponse response = authenticationService.refresh(request);

                assertThat(response.accessToken()).isEqualTo("new-access-token");
                assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
                assertThat(response.tokenType()).isEqualTo("Bearer");

                verify(refreshTokenService).rotateRefreshToken("old-refresh-token");
                verify(jwtService).generateAccessToken("diego@trackflow.com");
                verifyNoInteractions(authenticationManager, userRepository);
        }

        @Test
        void shouldNotGenerateAccessTokenWhenRefreshTokenIsInvalid() {
                RefreshTokenRequest request = new RefreshTokenRequest("invalid-refresh-token");

                when(refreshTokenService.rotateRefreshToken("invalid-refresh-token"))
                                .thenThrow(new InvalidRefreshTokenException());

                assertThatThrownBy(() -> authenticationService.refresh(request))
                                .isInstanceOf(InvalidRefreshTokenException.class);

                verify(jwtService, never()).generateAccessToken(any(String.class));
                verifyNoInteractions(authenticationManager, userRepository);
        }

        @Test
        void shouldReturnAuthenticatedUserWithRoles() {
                // Arrange
                String email = "diego@trackflow.com";

                RoleName roleName = RoleName.values()[0];
                Role role = Role.builder()
                                .id(1L)
                                .name(roleName)
                                .build();

                User user = User.builder()
                                .id(1L)
                                .name("Diego")
                                .email(email)
                                .roles(Set.of(role))
                                .build();

                when(userRepository.findByEmailIgnoreCase(email))
                                .thenReturn(java.util.Optional.of(user));

                // Act
                AuthenticatedUserResponse response = authenticationService.getAuthenticatedUser(email);

                // Assert
                assertThat(response.id()).isEqualTo(1L);
                assertThat(response.name()).isEqualTo("Diego");
                assertThat(response.email()).isEqualTo(email);
                assertThat(response.roles()).containsExactly(roleName.name());

                verify(userRepository).findByEmailIgnoreCase(email);
        }

        @Test
        void shouldReturnAuthenticatedUserWithoutRoles() {
                // Arrange
                String email = "diego@trackflow.com";

                User user = User.builder()
                                .id(1L)
                                .name("Diego")
                                .email(email)
                                .roles(Set.of())
                                .build();

                when(userRepository.findByEmailIgnoreCase(email))
                                .thenReturn(java.util.Optional.of(user));

                // Act
                AuthenticatedUserResponse response = authenticationService.getAuthenticatedUser(email);

                // Assert
                assertThat(response.id()).isEqualTo(1L);
                assertThat(response.name()).isEqualTo("Diego");
                assertThat(response.email()).isEqualTo(email);
                assertThat(response.roles()).isEmpty();
        }

        @Test
        void shouldThrowExceptionWhenAuthenticatedUserDoesNotExist() {
                // Arrange
                String email = "inexistente@trackflow.com";

                when(userRepository.findByEmailIgnoreCase(email))
                                .thenReturn(java.util.Optional.empty());

                // Act & Assert
                assertThatThrownBy(
                                () -> authenticationService.getAuthenticatedUser(email))
                                .isInstanceOf(UserNotFoundException.class)
                                .hasMessage("Usuário não encontrado.");

                verify(userRepository).findByEmailIgnoreCase(email);
        }

        @Test
        void shouldThrowExceptionWhenUserIsNotFoundAfterAuthentication() {
                // Arrange
                LoginRequest request = new LoginRequest(
                                "diego@trackflow.com",
                                "12345678");

                when(userRepository.findByEmailIgnoreCase(request.email()))
                                .thenReturn(java.util.Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> authenticationService.login(request))
                                .isInstanceOf(UserNotFoundException.class)
                                .hasMessage("Usuário não encontrado.");

                verify(authenticationManager).authenticate(
                                org.mockito.ArgumentMatchers.any(
                                                UsernamePasswordAuthenticationToken.class));

                verifyNoInteractions(jwtService, refreshTokenService);
        }
}
