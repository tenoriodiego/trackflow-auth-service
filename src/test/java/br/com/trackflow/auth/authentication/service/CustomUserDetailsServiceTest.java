package br.com.trackflow.auth.authentication.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import br.com.trackflow.auth.role.entity.Role;
import br.com.trackflow.auth.role.entity.RoleName;
import br.com.trackflow.auth.user.entity.User;
import br.com.trackflow.auth.user.repository.UserRepository;

class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        customUserDetailsService = new CustomUserDetailsService(userRepository);
    }

    @Test
    void shouldLoadUserByEmailSuccessfully() {

        Role customerRole = Role.builder()
                .id(1L)
                .name(RoleName.ROLE_CUSTOMER)
                .build();

        User user = User.builder()
                .id(1L)
                .name("José Diego")
                .email("diego@trackflow.com")
                .password("$2a$10$hashedPassword")
                .enabled(true)
                .roles(Set.of(customerRole))
                .build();

        when(userRepository.findByEmailIgnoreCase("diego@trackflow.com"))
                .thenReturn(java.util.Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(
                "diego@trackflow.com");

        assertThat(userDetails.getUsername())
                .isEqualTo("diego@trackflow.com");

        assertThat(userDetails.getPassword())
                .isEqualTo("$2a$10$hashedPassword");

        assertThat(userDetails.isEnabled())
                .isTrue();

        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_CUSTOMER");
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {

        when(userRepository.findByEmailIgnoreCase("naoexiste@trackflow.com"))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(
                        "naoexiste@trackflow.com"));
    }
}