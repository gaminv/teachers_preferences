package com.example.webapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.webapp.model.Role;
import com.example.webapp.model.Teacher;
import com.example.webapp.model.User;
import com.example.webapp.repository.TeacherRepository;
import com.example.webapp.repository.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository       userRepository;
    @Mock TeacherRepository    teacherRepository;
    @Mock PasswordEncoder      passwordEncoder;
    @Mock AuthenticationManager authManager;

    @InjectMocks AuthService authService;

    // Подставим секрет и время вручную
    @BeforeEach
    void init() throws Exception {
        // через Reflection, так как поле private
        var secretField = AuthService.class.getDeclaredField("jwtSecret");
        secretField.setAccessible(true);
        secretField.set(authService, "my-very-secret-key-which-is-long-enough");

        var expField = AuthService.class.getDeclaredField("jwtExpirationMs");
        expField.setAccessible(true);
        expField.setLong(authService, 3600000L); // 1h
    }

    @Test
    void register_success() {
        // arrange
        when(userRepository.existsBylogin("jdoe")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");

        // act
        User u = authService.register("John Doe", "jdoe", "pass");

        // assert
        assertNotNull(u);
        assertEquals("John Doe", u.getFullName());
        assertEquals("jdoe", u.getlogin());
        assertEquals(Role.TEACHER, u.getRole());

        // проверим, что репозитории сохраняли нужные сущности
        ArgumentCaptor<User> uc = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(uc.capture());
        assertEquals("encodedPass", uc.getValue().getPassword());

        ArgumentCaptor<Teacher> tc = ArgumentCaptor.forClass(Teacher.class);
        verify(teacherRepository).save(tc.capture());
        assertEquals(uc.getValue().getId(), tc.getValue().getUserId());
    }

    @Test
    void register_existingLogin_throws() {
        when(userRepository.existsBylogin("jdoe")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> authService.register("John", "jdoe", "pass")
        );
        assertEquals("login is already registered", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success() {
        // arrange
        String username = "jdoe", rawPass = "pass";
        User user = new User("John Doe", username, "encoded", Role.TEACHER);
        user.setlogin(username);
        user.setPasswordHash("encoded");
        // mock successful authentication
        Authentication authToken = new UsernamePasswordAuthenticationToken(username, rawPass);
        when(authManager.authenticate(any())).thenReturn(authToken);
        when(userRepository.findBylogin(username)).thenReturn(Optional.of(user));

        // act
        AuthService.AuthResponse resp = authService.login(username, rawPass);

        // assert
        assertNotNull(resp);
        assertNotNull(resp.token);
        assertEquals(username, resp.username);
        assertEquals(user.getFullName(), resp.fullName);
        assertEquals(Role.TEACHER, resp.role);

        // опционально, можно проверить: токен содержит subject=username
        var parsed  = Jwts.parserBuilder()
                          .setSigningKey(Keys.hmacShaKeyFor(
                                "my-very-secret-key-which-is-long-enough".getBytes(StandardCharsets.UTF_8)))
                          .build()
                          .parseClaimsJws(resp.token);
        assertEquals(username, parsed.getBody().getSubject());
    }
}
