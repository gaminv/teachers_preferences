package com.example.webapp.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

class TeacherModelUnitTest {

    @Test
    void constructorInitializesFields() {
        Teacher t = new Teacher(10L, "John Doe", "jdoe");
        assertEquals(10L, t.getUserId());
        assertEquals("John Doe", t.getName());
        assertEquals("jdoe", t.getContactLogin());
    }

    @ParameterizedTest
    @CsvSource({
        "1,Teacher A,ta",
        "2,Teacher B,tb",
        "3,Teacher C,tc",
        "4,Teacher D,td",
        "5,Teacher E,te",
        "6,Teacher F,tf",
        "7,Teacher G,tg",
        "8,Teacher H,th",
        "9,Teacher I,ti",
        "10,Teacher J,tj",
        "11,Teacher K,tk",
        "12,Teacher L,tl"
    })
    void setterGetterRoundtrip(long userId, String name, String login) {
        Teacher t = new Teacher();
        t.setUserId(userId);
        t.setName(name);
        t.setContactLogin(login);
        assertEquals(userId, t.getUserId());
        assertEquals(name, t.getName());
        assertEquals(login, t.getContactLogin());
    }
}
