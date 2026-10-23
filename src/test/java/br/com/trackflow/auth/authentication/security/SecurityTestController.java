
package br.com.trackflow.auth.authentication.security;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class SecurityTestController {

    @GetMapping("/api/test/protected")
    String protectedEndpoint(Authentication authentication) {
        return authentication.getName();
    }

    @GetMapping("/api/auth/login")
    String publicEndpoint() {
        return "public";
    }
}
