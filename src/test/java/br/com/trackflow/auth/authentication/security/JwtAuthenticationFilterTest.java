package br.com.trackflow.auth.authentication.security;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import br.com.trackflow.auth.authentication.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

        @Mock
        private JwtService jwtService;

        @Mock
        private UserDetailsService userDetailsService;

        @Mock
        private FilterChain filterChain;

        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @BeforeEach
        void setUp() {
                jwtAuthenticationFilter = new JwtAuthenticationFilter(
                                jwtService,
                                userDetailsService);

                SecurityContextHolder.clearContext();
        }

        @Test
        void shouldAuthenticateUserWhenTokenIsValid()
                        throws ServletException, IOException {

                String token = "valid-jwt-token";
                String email = "diego@trackflow.com";

                UserDetails userDetails = User
                                .withUsername(email)
                                .password("encoded-password")
                                .authorities("ROLE_CUSTOMER")
                                .build();

                when(jwtService.isTokenValid(token))
                                .thenReturn(true);

                when(jwtService.extractUsername(token))
                                .thenReturn(email);

                when(userDetailsService.loadUserByUsername(email))
                                .thenReturn(userDetails);

                MockHttpServletRequest request = new MockHttpServletRequest();

                request.addHeader(
                                "Authorization",
                                "Bearer " + token);

                MockHttpServletResponse response = new MockHttpServletResponse();

                jwtAuthenticationFilter.doFilter(
                                request,
                                response,
                                filterChain);

                Authentication authentication = SecurityContextHolder
                                .getContext()
                                .getAuthentication();

                assertThat(authentication).isNotNull();
                assertThat(authentication.getName())
                                .isEqualTo(email);
                assertThat(authentication.getAuthorities())
                                .extracting(authority -> authority.getAuthority())
                                .containsExactly("ROLE_CUSTOMER");

                verify(jwtService).isTokenValid(token);
                verify(jwtService).extractUsername(token);
                verify(userDetailsService)
                                .loadUserByUsername(email);

                verify(filterChain)
                                .doFilter(request, response);
        }

        @Test
        void shouldContinueFilterChainWhenAuthorizationHeaderIsMissing()
                        throws ServletException, IOException {

                MockHttpServletRequest request = new MockHttpServletRequest();

                MockHttpServletResponse response = new MockHttpServletResponse();

                jwtAuthenticationFilter.doFilter(
                                request,
                                response,
                                filterChain);

                assertThat(
                                SecurityContextHolder
                                                .getContext()
                                                .getAuthentication())
                                .isNull();

                verify(filterChain)
                                .doFilter(request, response);
        }

        @Test
        void shouldContinueFilterChainWhenAuthorizationHeaderIsInvalid()
                        throws ServletException, IOException {

                MockHttpServletRequest request = new MockHttpServletRequest();

                request.addHeader(
                                "Authorization",
                                "Basic some-token");

                MockHttpServletResponse response = new MockHttpServletResponse();

                jwtAuthenticationFilter.doFilter(
                                request,
                                response,
                                filterChain);

                assertThat(
                                SecurityContextHolder
                                                .getContext()
                                                .getAuthentication())
                                .isNull();

                verify(filterChain)
                                .doFilter(request, response);
        }

        @Test
        void shouldContinueFilterChainWhenTokenIsInvalid()
                        throws ServletException, IOException {

                String token = "invalid-jwt-token";

                when(jwtService.isTokenValid(token))
                                .thenReturn(false);

                MockHttpServletRequest request = new MockHttpServletRequest();

                request.addHeader(
                                "Authorization",
                                "Bearer " + token);

                MockHttpServletResponse response = new MockHttpServletResponse();

                jwtAuthenticationFilter.doFilter(
                                request,
                                response,
                                filterChain);

                assertThat(
                                SecurityContextHolder
                                                .getContext()
                                                .getAuthentication())
                                .isNull();

                verify(jwtService)
                                .isTokenValid(token);

                verify(filterChain)
                                .doFilter(request, response);
        }
}