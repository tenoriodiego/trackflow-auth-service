package br.com.trackflow.auth.authentication.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType) {
}