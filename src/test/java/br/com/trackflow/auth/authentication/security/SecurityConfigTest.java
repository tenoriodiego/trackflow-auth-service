
package br.com.trackflow.auth.authentication.security;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.trackflow.auth.authentication.service.JwtService;

@WebMvcTest(controllers = SecurityTestController.class)
@Import({
                SecurityConfig.class,
                JwtAuthenticationFilter.class
})
class SecurityConfigTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private JwtService jwtService;

        @MockitoBean
        private UserDetailsService userDetailsService;

        @Test
        void shouldReturnUnauthorizedWhenAccessingProtectedEndpointWithoutToken()
                        throws Exception {

                mockMvc.perform(get("/api/test/protected"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldAllowAccessToProtectedEndpointWithValidToken()
                        throws Exception {

                String token = "valid-jwt-token";
                String email = "diego@trackflow.com";

                UserDetails userDetails = User
                                .withUsername(email)
                                .password("encoded-password")
                                .authorities("ROLE_CUSTOMER")
                                .build();

                when(jwtService.isTokenValid(token)).thenReturn(true);
                when(jwtService.extractUsername(token)).thenReturn(email);
                when(userDetailsService.loadUserByUsername(email))
                                .thenReturn(userDetails);

                mockMvc.perform(get("/api/test/protected")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(content().string(email));
        }

        @Test
        void shouldAllowAccessToPublicEndpointWithoutToken()
                        throws Exception {

                mockMvc.perform(get("/api/auth/login"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("public"));
        }

        @RestController
        static class TestController {

                @GetMapping("/api/test/protected")
                String protectedEndpoint(
                                org.springframework.security.core.Authentication authentication) {
                        return authentication.getName();
                }

                @GetMapping("/api/auth/login")
                String publicEndpoint() {
                        return "public";
                }
        }
}
