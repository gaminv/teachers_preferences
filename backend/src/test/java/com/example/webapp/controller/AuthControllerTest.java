package com.example.webapp.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.webapp.repository.UserRepository;
import com.example.webapp.service.AuthService;
import com.example.webapp.service.AuthService.AuthResponse;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    // Заглушка для JwtAuthFilter и других security-зависимостей
    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("POST /api/auth/register → 200 OK + success message")
    void registerUser_success() throws Exception {
        // По умолчанию authService.register не бросает
        String body = """
            {
                "fullName":"Alice",
                "login":"alice",
                "password":"pwd123"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("{\"message\":\"User registered successfully\"}"));

        verify(authService).register("Alice", "alice", "pwd123");
    }

    @Test
    @DisplayName("POST /api/auth/register → 400 Bad Request on duplicate login")
    void registerUser_duplicateLogin() throws Exception {
        doThrow(new IllegalArgumentException("login is already registered"))
            .when(authService).register(any(), any(), any());

        String body = """
            {
                "fullName":"Bob",
                "login":"bob",
                "password":"pwd"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )
            .andExpect(status().isBadRequest())
            .andExpect(content().json("{\"message\":\"login is already registered\"}"));

        verify(authService).register("Bob", "bob", "pwd");
    }

    @Test
    @DisplayName("POST /api/auth/register → 500 Internal Server Error on unexpected")
    void registerUser_failure() throws Exception {
        doThrow(new RuntimeException("DB down"))
            .when(authService).register(any(), any(), any());

        String body = """
            {
                "fullName":"Eve",
                "login":"eve",
                "password":"pwd"
            }
            """;

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )
            .andExpect(status().isInternalServerError())
            .andExpect(content().json("{\"message\":\"Registration failed\"}"));

        verify(authService).register("Eve", "eve", "pwd");
    }

    @Test
    @DisplayName("POST /api/auth/login → 200 OK + AuthResponse JSON")
    void loginUser_success() throws Exception {
        AuthResponse resp = new AuthResponse(
            "token123", 5L, "jdoe", "John Doe", null
        );
        when(authService.login("jdoe","pwd")).thenReturn(resp);

        String body = """
            {
                "login":"jdoe",
                "password":"pwd"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("token123"))
            .andExpect(jsonPath("$.id").value(5))
            .andExpect(jsonPath("$.username").value("jdoe"))
            .andExpect(jsonPath("$.fullName").value("John Doe"));

        verify(authService).login("jdoe","pwd");
    }

    @Test
    @DisplayName("POST /api/auth/login → 401 Unauthorized on bad credentials")
    void loginUser_failure() throws Exception {
        when(authService.login("bad","creds"))
            .thenThrow(new AuthenticationException("fail") {});

        String body = """
            {
                "login":"bad",
                "password":"creds"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
            )
            .andExpect(status().isUnauthorized())
            .andExpect(content().json("{\"message\":\"Неверный логин или пароль\"}"));

        verify(authService).login("bad","creds");
    }
}
