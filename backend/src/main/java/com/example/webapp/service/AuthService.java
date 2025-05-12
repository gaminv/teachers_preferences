package com.example.webapp.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.webapp.model.Role;
import com.example.webapp.model.Teacher;
import com.example.webapp.model.User;
import com.example.webapp.repository.TeacherRepository;
import com.example.webapp.repository.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @org.springframework.beans.factory.annotation.Value("${jwt.secret}")
    private String jwtSecret;

    @org.springframework.beans.factory.annotation.Value("${jwt.expirationMs}")
    private long jwtExpirationMs;

    @Transactional
    public User register(String fullName, String login, String rawPassword) {
        if (userRepository.existsBylogin(login)) {
            throw new IllegalArgumentException("login is already registered");
        }
        String encoded = passwordEncoder.encode(rawPassword);

        User u = new User(fullName, login, encoded, Role.TEACHER);
        userRepository.save(u);

        Teacher t = new Teacher(u.getId(), fullName, login);
        teacherRepository.save(t);

        return u;
    }

    public AuthResponse login(String username, String password) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(username, password)
        );
        User user = userRepository.findBylogin(username)
                      .orElseThrow(() -> new IllegalStateException("User not found"));

        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtExpirationMs);
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        var key = Keys.hmacShaKeyFor(keyBytes);

        String token = Jwts.builder()
            .setSubject(user.getlogin())
            .claim("id", user.getId())
            .claim("fullName", user.getFullName())
            .claim("role", user.getRole().name())
            .setIssuedAt(now)
            .setExpiration(exp)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

        return new AuthResponse(token, user.getId(), user.getlogin(), user.getFullName(), user.getRole());
    }

    public static class AuthResponse {
        public String token;
        public Long id;
        public String username;
        public String fullName;
        public Role role;
        public AuthResponse(String token, Long id, String username, String fullName, Role role) {
            this.token = token;
            this.id = id;
            this.username = username;
            this.fullName = fullName;
            this.role = role;
        }
    }
}
