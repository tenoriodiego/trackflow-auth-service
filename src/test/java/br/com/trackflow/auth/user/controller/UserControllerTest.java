package br.com.trackflow.auth.user.controller;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.trackflow.auth.shared.exception.EmailAlreadyExistsException;
import br.com.trackflow.auth.user.dto.RegisterRequest;
import br.com.trackflow.auth.user.dto.UserResponse;
import br.com.trackflow.auth.user.service.UserService;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

        @Autowired
        private MockMvc mockMvc;

        private final ObjectMapper objectMapper = new ObjectMapper();

        @MockitoBean
        private UserService userService;

        @Test
        void shouldRegisterUserSuccessfully() throws Exception {

                RegisterRequest request = new RegisterRequest(
                                "José Diego",
                                "diego@trackflow.com",
                                "12345678");

                UserResponse response = new UserResponse(
                                1L,
                                "José Diego",
                                "diego@trackflow.com",
                                true);

                when(userService.register(any(RegisterRequest.class)))
                                .thenReturn(response);

                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("José Diego"))
                                .andExpect(jsonPath("$.email")
                                                .value("diego@trackflow.com"))
                                .andExpect(jsonPath("$.enabled").value(true));

                verify(userService).register(any(RegisterRequest.class));
        }

        @Test
        void shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {

                RegisterRequest request = new RegisterRequest(
                                "",
                                "email-invalido",
                                "123");

                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.error")
                                                .value("Validation error"))
                                .andExpect(jsonPath("$.messages.name")
                                                .value("Nome é obrigatório"))
                                .andExpect(jsonPath("$.messages.email")
                                                .value("E-mail inválido"))
                                .andExpect(jsonPath("$.messages.password")
                                                .value("Senha deve ter entre 8 e 100 caracteres"));

                verify(userService, never())
                                .register(any(RegisterRequest.class));
        }

        @Test
        void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {

                RegisterRequest request = new RegisterRequest(
                                "José Diego",
                                "diego@trackflow.com",
                                "12345678");

                when(userService.register(any(RegisterRequest.class)))
                                .thenThrow(
                                                new EmailAlreadyExistsException(
                                                                "diego@trackflow.com"));

                mockMvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.status").value(409))
                                .andExpect(jsonPath("$.error").value("Conflict"))
                                .andExpect(jsonPath("$.message")
                                                .value(
                                                                "Já existe um usuário cadastrado com o e-mail: "
                                                                                + "diego@trackflow.com"));

                verify(userService).register(any(RegisterRequest.class));
        }
}