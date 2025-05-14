package com.example.webapp.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;

import com.example.webapp.dto.PreferenceDto;
import com.example.webapp.model.Preference;
import com.example.webapp.model.Teacher;
import com.example.webapp.repository.PreferenceRepository;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private PreferenceRepository prefRepo;

    @InjectMocks
    private AdminService adminService;

    private Teacher tAlice;
    private Teacher tBob;
    private Preference pAlice;
    private Preference pBob;

    @BeforeEach
    void setUp() {
        // Два преподавателя
        tAlice = new Teacher(0L, "Alice Smith", "asmith");
        tBob   = new Teacher(0L, "Bob Johnson", "bjohnson");

        // Preference от Alice
        pAlice = new Preference();
        pAlice.setId(1L);
        pAlice.setTeacher(tAlice);
        pAlice.setType("semester");
        pAlice.setSubject("Physics");
        pAlice.setGroups("B2");
        pAlice.setDays("");
        pAlice.setDaysPriority(0);
        pAlice.setTimes("11:00");
        pAlice.setTimesPriority(2);
        // Добавляем пустые строки, чтобы switch не получил null
        pAlice.setLoadType("");
        pAlice.setBoardType("");
        pAlice.setFormat("");
        pAlice.setComments("");

        // Preference от Bob
        pBob = new Preference();
        pBob.setId(2L);
        pBob.setTeacher(tBob);
        pBob.setType("semester");
        pBob.setSubject("Math");
        pBob.setGroups("A1");
        pBob.setDays("Mon, Wed");
        pBob.setDaysPriority(5);
        pBob.setTimes("09:00");
        pBob.setTimesPriority(3);
        pBob.setLoadType("");
        pBob.setBoardType("");
        pBob.setFormat("");
        pBob.setComments("");
    }

    @Test
    void getAllPreferences_returnsDtoList() {
        when(prefRepo.findAll()).thenReturn(Arrays.asList(pBob, pAlice));

        List<PreferenceDto> dtos = adminService.getAllPreferences();

        assertEquals(2, dtos.size());

        PreferenceDto dtoAlice = dtos.stream()
            .filter(d -> "Alice Smith".equals(d.teacherName))
            .findFirst().orElseThrow();
        assertEquals(1L, dtoAlice.id);
        assertEquals("Physics", dtoAlice.subject);
        assertTrue(dtoAlice.days.isEmpty());

        PreferenceDto dtoBob = dtos.stream()
            .filter(d -> "Bob Johnson".equals(d.teacherName))
            .findFirst().orElseThrow();
        assertEquals(2L, dtoBob.id);
        assertEquals("Math", dtoBob.subject);
        assertEquals(Arrays.asList("Mon", "Wed"), dtoBob.days);

        verify(prefRepo).findAll();
    }

    @Test
    void exportPreferencesToExcel_createsValidWorkbook() throws IOException {
        when(prefRepo.findAll()).thenReturn(Arrays.asList(pBob, pAlice));

        ByteArrayResource resource = adminService.exportPreferencesToExcel();
        byte[] data = resource.getByteArray();
        assertTrue(data.length > 0, "Excel должен содержать данные");

        try (var wb = new XSSFWorkbook(new ByteArrayInputStream(data))) {
            Sheet sheet = wb.getSheet("Пожелания");
            assertNotNull(sheet);

            Row header = sheet.getRow(0);
            assertEquals("Преподаватель", header.getCell(0).getStringCellValue());
            assertEquals("Логин",           header.getCell(1).getStringCellValue());
            assertEquals("Тип",             header.getCell(2).getStringCellValue());
            assertEquals("Предмет",         header.getCell(3).getStringCellValue());

            // Ожидаем сортировку: Alice → Bob
            Row row1 = sheet.getRow(1);
            assertEquals("Alice Smith", row1.getCell(0).getStringCellValue());
            assertEquals("asmith",       row1.getCell(1).getStringCellValue());

            Row row2 = sheet.getRow(2);
            assertEquals("Bob Johnson", row2.getCell(0).getStringCellValue());
            assertEquals("bjohnson",     row2.getCell(1).getStringCellValue());
        }

        verify(prefRepo).findAll();
    }
}
