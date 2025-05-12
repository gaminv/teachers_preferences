package com.example.webapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.webapp.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBylogin(String login);
    boolean existsBylogin(String login);
}
