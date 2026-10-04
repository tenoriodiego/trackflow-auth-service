package br.com.trackflow.auth.authentication.security;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;

@SpringBootTest
class AuthenticationManagerTest {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Test
    void shouldLoadAuthenticationManager() {
        assertThat(authenticationManager)
                .isNotNull();
    }

}