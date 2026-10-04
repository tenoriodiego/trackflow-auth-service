package br.com.trackflow.auth.authentication.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import br.com.trackflow.auth.authentication.service.JwtService;
import jakarta.servlet.FilterChain;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private UserDetailsService userDetailsService;
    private JwtAuthenticationFilter filter;

    private FilterChain filterChain;

    @BeforeEach
    void setUp() {

        jwtService = mock(JwtService.class);
        userDetailsService = mock(UserDetailsService.class);

        filter = new JwtAuthenticationFilter(
                jwtService,
                userDetailsService);

        filterChain = mock(FilterChain.class);
    }

    @Test
    void shouldContinueChainWhenAuthorizationHeaderIsMissing()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();

        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(
                request,
                response,
                filterChain);

        verify(filterChain)
                .doFilter(request, response);

        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void shouldContinueChainWhenTokenIsInvalid()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer invalid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isTokenValid("invalid-token"))
                .thenReturn(false);

        filter.doFilter(
                request,
                response,
                filterChain);

        verify(filterChain)
                .doFilter(request, response);

        verify(jwtService)
                .isTokenValid("invalid-token");

        verifyNoInteractions(userDetailsService);
    }

    @Test
    void shouldAuthenticateUserWhenTokenIsValid()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addHeader(
                "Authorization",
                "Bearer valid-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDetails userDetails = User.withUsername("diego@trackflow.com")
                .password("password")
                .authorities("ROLE_CUSTOMER")
                .build();

        when(jwtService.isTokenValid("valid-token"))
                .thenReturn(true);

        when(jwtService.extractUsername("valid-token"))
                .thenReturn("diego@trackflow.com");

        when(userDetailsService.loadUserByUsername(
                "diego@trackflow.com")).thenReturn(userDetails);

        filter.doFilter(
                request,
                response,
                filterChain);

        verify(userDetailsService)
                .loadUserByUsername(
                        "diego@trackflow.com");

        verify(filterChain)
                .doFilter(request, response);
    }
}