package br.com.trackflow.auth.authentication.security;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.trackflow.auth.authentication.service.AuthenticationService;
import br.com.trackflow.auth.authentication.service.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class SecurityIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private JwtService jwtService;

        @MockitoBean
        private UserDetailsService userDetailsService;

        @MockitoBean
        private AuthenticationService authenticationService;

        @Test
        void shouldReturnUnauthorizedWhenAccessingProtectedEndpointWithoutToken()
                        throws Exception {

                mockMvc.perform(
                                get("/api/auth/me")
                                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturnUnauthorizedWhenAccessingProtectedEndpointWithInvalidToken()
                        throws Exception {

                when(jwtService.isTokenValid("invalid-token"))
                                .thenReturn(false);

                mockMvc.perform(
                                get("/api/auth/me")
                                                .header("Authorization", "Bearer invalid-token")
                                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldAllowAccessWhenTokenIsValid()
                        throws Exception {

                String token = "valid-token";
                String email = "diego@trackflow.com";

                var userDetails = User.withUsername(email)
                                .password("encoded-password")
                                .authorities("ROLE_CUSTOMER")
                                .build();

                when(jwtService.isTokenValid(token))
                                .thenReturn(true);

                when(jwtService.extractUsername(token))
                                .thenReturn(email);

                when(userDetailsService.loadUserByUsername(email))
                                .thenReturn(userDetails);

                when(authenticationService.getAuthenticatedUser(email))
                                .thenReturn(
                                                new br.com.trackflow.auth.user.dto.AuthenticatedUserResponse(
                                                                1L,
                                                                "José Diego",
                                                                email,
                                                                Set.of("ROLE_CUSTOMER")));

                mockMvc.perform(
                                get("/api/auth/me")
                                                .header("Authorization", "Bearer " + token)
                                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());
        }
}