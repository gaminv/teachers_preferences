package com.example.webapp.controller;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.webapp.dto.PreferenceDto;
import com.example.webapp.repository.UserRepository;
import com.example.webapp.service.AdminService;

@WebMvcTest(controllers = AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    // Чтобы контекст не падал из-за JwtAuthFilter
    @MockBean
    private UserRepository userRepository;

    @Test
    @DisplayName("GET /api/admin/preferences → 200 + JSON списка DTO")
    void getAllPreferences_returnsList() throws Exception {
        PreferenceDto dto1 = new PreferenceDto();
        dto1.id = 10L;
        dto1.teacherName = "Alice";
        dto1.subject = "Math";

        PreferenceDto dto2 = new PreferenceDto();
        dto2.id = 20L;
        dto2.teacherName = "Bob";
        dto2.subject = "Physics";

        when(adminService.getAllPreferences()).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/admin/preferences"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(10))
            .andExpect(jsonPath("$[0].teacherName").value("Alice"))
            .andExpect(jsonPath("$[1].id").value(20))
            .andExpect(jsonPath("$[1].teacherName").value("Bob"));

        verify(adminService).getAllPreferences();
    }

    @Test
    @DisplayName("GET /api/admin/preferences/export → 200 + Excel resource")
    void exportPreferencesToExcel_returnsExcel() throws Exception {
        byte[] fakeExcel = new byte[] {1, 2, 3, 4, 5};
        ByteArrayResource resource = new ByteArrayResource(fakeExcel);

        when(adminService.exportPreferencesToExcel()).thenReturn(resource);

        mockMvc.perform(get("/api/admin/preferences/export"))
            .andExpect(status().isOk())
            .andExpect(header().string(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=preferences.xlsx"
            ))
            .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
            .andExpect(content().bytes(fakeExcel));

        verify(adminService).exportPreferencesToExcel();
    }
}
