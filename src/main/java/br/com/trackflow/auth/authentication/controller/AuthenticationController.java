package br.com.trackflow.auth.authentication.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import br.com.trackflow.auth.authentication.dto.LoginRequest;
import br.com.trackflow.auth.authentication.dto.LoginResponse;
import br.com.trackflow.auth.authentication.service.AuthenticationService;
import br.com.trackflow.auth.user.dto.AuthenticatedUserResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authenticationService.login(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUserResponse> me(
            Authentication authentication) {

        AuthenticatedUserResponse response = authenticationService.getAuthenticatedUser(
                authentication.getName());

        return ResponseEntity.ok(response);
    }
}