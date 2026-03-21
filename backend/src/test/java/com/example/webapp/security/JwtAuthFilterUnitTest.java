package com.example.webapp.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.webapp.model.Role;
import com.example.webapp.model.User;
import com.example.webapp.repository.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterUnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtAuthFilter filter;

    private static final String SECRET = "my-very-secret-key-which-is-long-enough-123";

    @BeforeEach
    void init() throws Exception {
        SecurityContextHolder.clearContext();
        Field field = JwtAuthFilter.class.getDeclaredField("jwtSecret");
        field.setAccessible(true);
        field.set(filter, SECRET);
    }

    private String tokenFor(String login) {
        var key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(login)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void setsAuthenticationForValidTokenAndExistingUser() throws Exception {
        User user = new User("John", "john", "hash", Role.TEACHER);
        when(userRepository.findBylogin("john")).thenReturn(Optional.of(user));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + tokenFor("john"));
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void keepsAnonymousWhenUserNotFound() throws Exception {
        when(userRepository.findBylogin("ghost")).thenReturn(Optional.empty());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + tokenFor("ghost"));
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "Bearer ",
            "Bearer invalid.token",
            "Token abc",
            "Basic qwerty"
    })
    void invalidOrMissingTokenDoesNotAuthenticate(String header) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (!header.isEmpty()) {
            request.addHeader("Authorization", header);
        }
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
