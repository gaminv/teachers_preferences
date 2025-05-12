package com.example.webapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "teachers")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Ссылка на users.id */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "contact_login", unique = true, nullable = false)
    private String contactlogin;

    public Teacher() {}

    public Teacher(Long userId, String name, String contactlogin) {
        this.userId = userId;
        this.name = name;
        this.contactlogin = contactlogin;
    }

    // --- Геттеры и сеттеры ---
    public Long getId() { return id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContactLogin() { return contactlogin; }
    public void setContactLogin(String contactlogin) { this.contactlogin = contactlogin; }
}
