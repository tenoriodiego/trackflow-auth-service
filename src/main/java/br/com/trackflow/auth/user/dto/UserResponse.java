package br.com.trackflow.auth.user.dto;

public record UserResponse(
        Long id,
        String name,
        String email,
        boolean enabled) {
}