package br.com.trackflow.auth;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.trackflow.auth.role.entity.Role;
import br.com.trackflow.auth.role.entity.RoleName;
import br.com.trackflow.auth.role.repository.RoleRepository;
import br.com.trackflow.auth.shared.exception.EmailAlreadyExistsException;
import br.com.trackflow.auth.shared.exception.RoleNotFoundException;
import br.com.trackflow.auth.user.dto.RegisterRequest;
import br.com.trackflow.auth.user.dto.UserResponse;
import br.com.trackflow.auth.user.entity.User;
import br.com.trackflow.auth.user.repository.UserRepository;
import br.com.trackflow.auth.user.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldRegisterUserSuccessfully() {

        RegisterRequest request = new RegisterRequest(
                "José Diego",
                " DIEGO@TRACKFLOW.COM ",
                "12345678");

        Role customerRole = Role.builder()
                .id(1L)
                .name(RoleName.ROLE_CUSTOMER)
                .build();

        User savedUser = User.builder()
                .id(1L)
                .name("José Diego")
                .email("diego@trackflow.com")
                .password("encoded-password")
                .enabled(true)
                .roles(java.util.Set.of(customerRole))
                .build();

        when(userRepository.existsByEmailIgnoreCase("diego@trackflow.com"))
                .thenReturn(false);

        when(roleRepository.findByName(RoleName.ROLE_CUSTOMER))
                .thenReturn(Optional.of(customerRole));

        when(passwordEncoder.encode("12345678"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        UserResponse response = userService.register(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("José Diego");
        assertThat(response.email()).isEqualTo("diego@trackflow.com");
        assertThat(response.enabled()).isTrue();

        verify(userRepository)
                .existsByEmailIgnoreCase("diego@trackflow.com");

        verify(roleRepository)
                .findByName(RoleName.ROLE_CUSTOMER);

        verify(passwordEncoder)
                .encode("12345678");

        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        RegisterRequest request = new RegisterRequest(
                "José Diego",
                "diego@trackflow.com",
                "12345678");

        when(userRepository.existsByEmailIgnoreCase("diego@trackflow.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Já existe um usuário cadastrado com o e-mail: diego@trackflow.com");

        verify(userRepository)
                .existsByEmailIgnoreCase("diego@trackflow.com");

        verify(roleRepository, never())
                .findByName(any());

        verify(passwordEncoder, never())
                .encode(any());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenCustomerRoleDoesNotExist() {

        RegisterRequest request = new RegisterRequest(
                "José Diego",
                "diego@trackflow.com",
                "12345678");

        when(userRepository.existsByEmailIgnoreCase("diego@trackflow.com"))
                .thenReturn(false);

        when(roleRepository.findByName(RoleName.ROLE_CUSTOMER))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(RoleNotFoundException.class)
                .hasMessage("Role não encontrada: ROLE_CUSTOMER");

        verify(userRepository)
                .existsByEmailIgnoreCase("diego@trackflow.com");

        verify(roleRepository)
                .findByName(RoleName.ROLE_CUSTOMER);

        verify(passwordEncoder, never())
                .encode(any());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void shouldEncodePasswordBeforeSaving() {

        RegisterRequest request = new RegisterRequest(
                "José Diego",
                "diego@trackflow.com",
                "12345678");

        Role customerRole = Role.builder()
                .id(1L)
                .name(RoleName.ROLE_CUSTOMER)
                .build();

        when(userRepository.existsByEmailIgnoreCase("diego@trackflow.com"))
                .thenReturn(false);

        when(roleRepository.findByName(RoleName.ROLE_CUSTOMER))
                .thenReturn(Optional.of(customerRole));

        when(passwordEncoder.encode("12345678"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.register(request);

        verify(passwordEncoder)
                .encode("12345678");

        verify(userRepository).save(argThat(user -> user.getPassword().equals("encoded-password")));
    }

    @Test
    void shouldNormalizeEmailBeforeSaving() {

        RegisterRequest request = new RegisterRequest(
                "José Diego",
                "  DIEGO@TRACKFLOW.COM  ",
                "12345678");

        Role customerRole = Role.builder()
                .id(1L)
                .name(RoleName.ROLE_CUSTOMER)
                .build();

        when(userRepository.existsByEmailIgnoreCase("diego@trackflow.com"))
                .thenReturn(false);

        when(roleRepository.findByName(RoleName.ROLE_CUSTOMER))
                .thenReturn(Optional.of(customerRole));

        when(passwordEncoder.encode("12345678"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.register(request);

        assertThat(response.email())
                .isEqualTo("diego@trackflow.com");

        verify(userRepository)
                .existsByEmailIgnoreCase("diego@trackflow.com");

        verify(userRepository).save(argThat(user -> user.getEmail().equals("diego@trackflow.com")));
    }

    @Test
    void shouldAssignCustomerRoleToNewUser() {

        RegisterRequest request = new RegisterRequest(
                "José Diego",
                "diego@trackflow.com",
                "12345678");

        Role customerRole = Role.builder()
                .id(1L)
                .name(RoleName.ROLE_CUSTOMER)
                .build();

        when(userRepository.existsByEmailIgnoreCase("diego@trackflow.com"))
                .thenReturn(false);

        when(roleRepository.findByName(RoleName.ROLE_CUSTOMER))
                .thenReturn(Optional.of(customerRole));

        when(passwordEncoder.encode("12345678"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.register(request);

        verify(userRepository).save(argThat(user -> user.getRoles().contains(customerRole)
                && user.getRoles().size() == 1));
    }
}