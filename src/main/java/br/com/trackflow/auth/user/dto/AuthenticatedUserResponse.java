package br.com.trackflow.auth.user.dto;

import java.util.Set;

public record AuthenticatedUserResponse(
        Long id,
        String name,
        String email,
        Set<String> roles) {
}