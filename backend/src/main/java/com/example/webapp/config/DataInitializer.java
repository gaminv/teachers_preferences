package com.example.webapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.example.webapp.model.Role;
import com.example.webapp.model.User;
import com.example.webapp.repository.UserRepository;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        String adminLogin = "admin";
        String adminPassword = "admin1";

        // Если админа ещё нет в таблице users — создаём его
        if (!userRepository.existsBylogin(adminLogin)) {
            User admin = new User(
                "Администратор",                 
                adminLogin,                      
                passwordEncoder.encode(adminPassword), 
                Role.ADMIN                       
            );
            userRepository.save(admin);
            System.out.println("==> Создан пользователь admin/admin с ролью ADMIN");
        }
    }
}
