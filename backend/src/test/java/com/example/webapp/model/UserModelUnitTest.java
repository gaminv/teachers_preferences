package com.example.webapp.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;

class UserModelUnitTest {

    @Test
    void constructorAndGettersWork() {
        User u = new User("John Doe", "jdoe", "hash", Role.TEACHER);
        assertEquals("John Doe", u.getFullName());
        assertEquals("jdoe", u.getlogin());
        assertEquals("hash", u.getPasswordHash());
        assertEquals(Role.TEACHER, u.getRole());
    }

    @ParameterizedTest
    @CsvSource({
        "Alice,alice,hash1",
        "Bob,bob,hash2",
        "Иван Иванов,ivan,hash3",
        "Teacher A,t_a,hash4",
        "Teacher B,t_b,hash5",
        "Teacher C,t_c,hash6",
        "Teacher D,t_d,hash7",
        "Teacher E,t_e,hash8"
    })
    void settersRoundtrip(String name, String login, String hash) {
        User u = new User();
        u.setFullName(name);
        u.setlogin(login);
        u.setPasswordHash(hash);
        u.setRole(Role.ADMIN);
        assertEquals(name, u.getFullName());
        assertEquals(login, u.getlogin());
        assertEquals(hash, u.getPasswordHash());
        assertEquals("ROLE_ADMIN", u.getAuthorities().iterator().next().getAuthority());
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void authoritiesReflectRole(Role role) {
        User u = new User("X", "x", "h", role);
        assertEquals("ROLE_" + role.name(), u.getAuthorities().iterator().next().getAuthority());
    }

    @ParameterizedTest
    @CsvSource({
        "true,true,true,true",
        "true,true,true,false",
        "true,true,false,true",
        "true,false,true,true",
        "false,true,true,true"
    })
    void accountFlagsAreAlwaysTrue(boolean a, boolean b, boolean c, boolean d) {
        User u = new User("X", "x", "h", Role.TEACHER);
        assertTrue(u.isAccountNonExpired());
        assertTrue(u.isAccountNonLocked());
        assertTrue(u.isCredentialsNonExpired());
        assertTrue(u.isEnabled());
    }

    @Test
    void usernameAndPasswordMappedCorrectly() {
        User u = new User("X", "login1", "hash-x", Role.TEACHER);
        assertEquals("login1", u.getUsername());
        assertEquals("hash-x", u.getPassword());
    }
}
