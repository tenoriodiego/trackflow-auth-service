package br.com.trackflow.auth.authentication.service;

import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.trackflow.auth.authentication.dto.LoginRequest;
import br.com.trackflow.auth.authentication.dto.LoginResponse;
import br.com.trackflow.auth.user.dto.AuthenticatedUserResponse;
import br.com.trackflow.auth.user.entity.User;
import br.com.trackflow.auth.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

        private final AuthenticationManager authenticationManager;
        private final JwtService jwtService;
        private final RefreshTokenService refreshTokenService;
        private final UserRepository userRepository;

        public LoginResponse login(LoginRequest request) {

                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                request.email(),
                                                request.password()));

                User user = userRepository.findByEmailIgnoreCase(request.email())
                                .orElseThrow();

                String accessToken = jwtService.generateAccessToken(user.getEmail());

                String refreshToken = refreshTokenService.createRefreshToken(user);

                return new LoginResponse(
                                accessToken,
                                refreshToken,
                                "Bearer");
        }

        @Transactional(readOnly = true)
        public AuthenticatedUserResponse getAuthenticatedUser(String email) {

                User user = userRepository.findByEmailIgnoreCase(email)
                                .orElseThrow();

                return new AuthenticatedUserResponse(
                                user.getId(),
                                user.getName(),
                                user.getEmail(),
                                user.getRoles()
                                                .stream()
                                                .map(role -> role.getName().name())
                                                .collect(Collectors.toSet()));
        }
}