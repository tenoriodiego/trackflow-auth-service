
package br.com.trackflow.auth.shared.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() {
        // Arrange
        UserNotFoundException exception = new UserNotFoundException("Usuário não encontrado.");

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleUserNotFound(exception);

        // Assert
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(response.getBody())
                .containsEntry("status", HttpStatus.NOT_FOUND.value())
                .containsEntry("error", "Not Found")
                .containsEntry("message", "Usuário não encontrado.")
                .containsKey("timestamp");
    }

    @Test
    void shouldReturnConflictWhenEmailAlreadyExists() {
        // Arrange
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException("E-mail já cadastrado.");

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleEmailAlreadyExists(exception);

        // Assert
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(response.getBody())
                .containsEntry("status", HttpStatus.CONFLICT.value())
                .containsEntry("error", "Conflict")
                .containsEntry("message", exception.getMessage());
    }

    @Test
    void shouldReturnUnauthorizedWhenCredentialsAreInvalid() {
        // Arrange
        org.springframework.security.authentication.BadCredentialsException exception = new org.springframework.security.authentication.BadCredentialsException(
                "Bad credentials");

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleBadCredentials(exception);

        // Assert
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        assertThat(response.getBody())
                .containsEntry("status", HttpStatus.UNAUTHORIZED.value())
                .containsEntry("error", "Unauthorized")
                .containsEntry("message", "E-mail ou senha inválidos.");
    }

    @Test
    void shouldReturnUnauthorizedWhenRefreshTokenIsInvalid() {
        // Arrange
        br.com.trackflow.auth.authentication.exception.InvalidRefreshTokenException exception = new br.com.trackflow.auth.authentication.exception.InvalidRefreshTokenException();

        // Act
        ResponseEntity<Map<String, Object>> response = handler.handleInvalidRefreshToken(exception);

        // Assert
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        assertThat(response.getBody())
                .containsEntry("status", HttpStatus.UNAUTHORIZED.value())
                .containsEntry("error", "Unauthorized")
                .containsEntry("message", exception.getMessage());
    }

}
