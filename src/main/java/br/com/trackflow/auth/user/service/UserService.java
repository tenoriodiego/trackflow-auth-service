package br.com.trackflow.auth.user.service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.trackflow.auth.role.entity.Role;
import br.com.trackflow.auth.role.entity.RoleName;
import br.com.trackflow.auth.role.repository.RoleRepository;
import br.com.trackflow.auth.shared.exception.EmailAlreadyExistsException;
import br.com.trackflow.auth.shared.exception.RoleNotFoundException;
import br.com.trackflow.auth.user.dto.RegisterRequest;
import br.com.trackflow.auth.user.dto.UserResponse;
import br.com.trackflow.auth.user.entity.User;
import br.com.trackflow.auth.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(RegisterRequest request) {

        String email = normalizeEmail(request.email());

        validateEmailAlreadyExists(email);

        Role customerRole = findCustomerRole();

        User user = buildUser(request, email, customerRole);

        User savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    private void validateEmailAlreadyExists(String email) {

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyExistsException(email);
        }
    }

    private Role findCustomerRole() {

        return roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseThrow(() -> new RoleNotFoundException(RoleName.ROLE_CUSTOMER));
    }

    private User buildUser(
            RegisterRequest request,
            String email,
            Role customerRole) {

        LocalDateTime now = LocalDateTime.now();

        return User.builder()
                .name(request.name().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .enabled(true)
                .createdAt(now)
                .updatedAt(now)
                .roles(Set.of(customerRole))
                .build();
    }

    private String normalizeEmail(String email) {

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private UserResponse toResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.isEnabled());
    }
}