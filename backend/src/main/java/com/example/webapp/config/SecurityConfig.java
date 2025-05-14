package com.example.webapp.config;

import com.example.webapp.repository.UserRepository;
import com.example.webapp.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.http.HttpMethod;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private UserRepository userRepository;

    // Password encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // UserDetailsService через UserRepository
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findBylogin(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    // DaoAuthenticationProvider
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Основная конфигурация безопасности
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // включаем CORS
            .cors()
        .and()
            // отключаем CSRF для stateless REST
            .csrf().disable()
            // права доступа
            .authorizeHttpRequests()
                .requestMatchers("/error").permitAll() 
                // разрешаем всем preflight и OPTIONS-запросы
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // регистрация/логин
                .requestMatchers("/api/auth/**").permitAll()
                // только учитель
                .requestMatchers("/api/teacher/**").hasRole("TEACHER")
                // только админ
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // всё остальное требует авторизации
                .anyRequest().authenticated()
        .and()
            // stateless сессия (JWT)
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
            // наш провайдер
            .authenticationProvider(authenticationProvider())
            // JWT-фильтр перед UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Глобальная CORS-настройка для всего API
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // применяется ко всем эндпоинтам
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
