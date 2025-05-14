package com.example.webapp.controller;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.webapp.dto.PreferenceDto;
import com.example.webapp.model.Role;
import com.example.webapp.model.User;
import com.example.webapp.repository.UserRepository;
import com.example.webapp.service.TeacherService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = TeacherController.class)
@AutoConfigureMockMvc(addFilters = false)
class TeacherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TeacherService teacherService;

    // Добавляем заглушку, чтобы Context не падал на JwtAuthFilter
    @MockBean
    private UserRepository userRepository;

    /**
     * Создаёт Authentication с нашим UserDetails, у которого через рефлексию
     * установлено приватное поле id.
     */
    private Authentication authWithUser(Long id) throws Exception {
        User u = new User("John Doe", "jdoe", "pwd", Role.TEACHER);
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(u, id);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(u);
        return auth;
    }

    @Test
    @DisplayName("GET /api/teacher/preferences → 200 OK + пустой список")
    void getPreferences_empty() throws Exception {
        when(teacherService.getPreferences(42L, "semester"))
            .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/teacher/preferences")
                .param("type", "semester")
                .principal(authWithUser(42L))
            )
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("[]"));

        verify(teacherService).getPreferences(42L, "semester");
    }

    @Test
    @DisplayName("POST /api/teacher/preferences → 200 OK + возвращает переданный DTO")
    void savePreferences_success() throws Exception {
        PreferenceDto dto = new PreferenceDto();
        dto.id = 7L;
        dto.type = "session";
        dto.subject = "Math";

        when(teacherService.savePreferences(eq(99L), anyList()))
            .thenReturn(List.of(dto));

        String json = """
            [ { 
                "id": 7, 
                "type": "session", 
                "subject": "Math" 
              } ]
            """;

        mockMvc.perform(post("/api/teacher/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                .principal(authWithUser(99L))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(7))
            .andExpect(jsonPath("$[0].type").value("session"))
            .andExpect(jsonPath("$[0].subject").value("Math"));

        verify(teacherService).savePreferences(eq(99L), anyList());
    }
}
