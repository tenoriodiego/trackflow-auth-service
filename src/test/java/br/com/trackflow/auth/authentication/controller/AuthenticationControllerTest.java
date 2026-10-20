
package br.com.trackflow.auth.authentication.controller;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.trackflow.auth.authentication.dto.LoginRequest;
import br.com.trackflow.auth.authentication.dto.LoginResponse;
import br.com.trackflow.auth.authentication.dto.RefreshTokenRequest;
import br.com.trackflow.auth.authentication.exception.InvalidRefreshTokenException;
import br.com.trackflow.auth.authentication.service.AuthenticationService;
import br.com.trackflow.auth.authentication.service.JwtService;
import br.com.trackflow.auth.user.dto.AuthenticatedUserResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.Import;
import br.com.trackflow.auth.shared.exception.GlobalExceptionHandler;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthenticationControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private AuthenticationService authenticationService;

        @MockitoBean
        private JwtService jwtService;

        private final ObjectMapper objectMapper = new ObjectMapper();

        @Test
        void shouldLoginSuccessfully() throws Exception {
                LoginRequest request = new LoginRequest(
                                "diego@trackflow.com",
                                "12345678");

                LoginResponse response = new LoginResponse(
                                "jwt-token",
                                null,
                                "Bearer");

                when(authenticationService.login(any(LoginRequest.class)))
                                .thenReturn(response);

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                                .andExpect(jsonPath("$.tokenType").value("Bearer"));
        }

        @Test
        void shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {
                LoginRequest request = new LoginRequest("", "");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnUnauthorizedWhenCredentialsAreInvalid()
                        throws Exception {
                LoginRequest request = new LoginRequest(
                                "diego@trackflow.com",
                                "senha-incorreta");

                when(authenticationService.login(any(LoginRequest.class)))
                                .thenThrow(
                                                new org.springframework.security.authentication.BadCredentialsException(
                                                                "Bad credentials"));

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.message")
                                                .value("E-mail ou senha inválidos."));
        }

        @Test
        void shouldReturnAuthenticatedUser() throws Exception {
                AuthenticatedUserResponse response = new AuthenticatedUserResponse(
                                1L,
                                "José Diego",
                                "diego@trackflow.com",
                                Set.of("ROLE_CUSTOMER"));

                when(authenticationService.getAuthenticatedUser(
                                "diego@trackflow.com"))
                                .thenReturn(response);

                mockMvc.perform(get("/api/auth/me")
                                .principal(new UsernamePasswordAuthenticationToken(
                                                "diego@trackflow.com", null)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("José Diego"))
                                .andExpect(jsonPath("$.email")
                                                .value("diego@trackflow.com"))
                                .andExpect(jsonPath("$.roles[0]")
                                                .value("ROLE_CUSTOMER"));
        }

        @Test
        void shouldRefreshTokensSuccessfully() throws Exception {
                RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");

                LoginResponse response = new LoginResponse(
                                "new-access-token",
                                "new-refresh-token",
                                "Bearer");

                when(authenticationService.refresh(any(RefreshTokenRequest.class)))
                                .thenReturn(response);

                mockMvc.perform(post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken")
                                                .value("new-access-token"))
                                .andExpect(jsonPath("$.refreshToken")
                                                .value("new-refresh-token"))
                                .andExpect(jsonPath("$.tokenType").value("Bearer"));
        }

        @Test
        void shouldReturnUnauthorizedWhenRefreshTokenIsInvalid()
                        throws Exception {
                RefreshTokenRequest request = new RefreshTokenRequest("invalid-refresh-token");

                when(authenticationService.refresh(any(RefreshTokenRequest.class)))
                                .thenThrow(new InvalidRefreshTokenException());

                mockMvc.perform(post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.status").value(401))
                                .andExpect(jsonPath("$.message")
                                                .value("Refresh token inválido ou expirado."));
        }

        @Test
        void shouldReturnBadRequestWhenRefreshTokenIsBlank()
                        throws Exception {
                RefreshTokenRequest request = new RefreshTokenRequest("");

                mockMvc.perform(post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }
}
