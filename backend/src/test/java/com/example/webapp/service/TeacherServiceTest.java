package com.example.webapp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.webapp.dto.PreferenceDto;
import com.example.webapp.model.Preference;
import com.example.webapp.model.Teacher;
import com.example.webapp.repository.PreferenceRepository;
import com.example.webapp.repository.TeacherRepository;

@ExtendWith(MockitoExtension.class)
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepo;

    @Mock
    private PreferenceRepository prefRepo;

    @InjectMocks
    private TeacherService teacherService;

    private Teacher teacher;

    @BeforeEach
    void setUp() {
        // общий преподавательный объект
        teacher = new Teacher(42L, "John Doe", "jdoe");
        teacher.setUserId(42L);
    }

    @Test
    void getPreferences_success() {
        // arrange
        Preference p1 = new Preference();
        p1.setId(1L);
        p1.setTeacher(teacher);
        p1.setType("semester");
        p1.setSubject("Math");
        p1.setGroups("A1");
        p1.setDays("Mon, Tue");
        p1.setDaysPriority(10);
        p1.setTimes("08:00");
        p1.setTimesPriority(5);

        Preference p2 = new Preference();
        p2.setId(2L);
        p2.setTeacher(teacher);
        p2.setType("semester");
        p2.setSubject("Physics");
        p2.setGroups("B2");
        p2.setDays("");
        p2.setDaysPriority(0);
        p2.setTimes("10:00");
        p2.setTimesPriority(3);

        when(teacherRepo.findByUserId(42L)).thenReturn(Optional.of(teacher));
        when(prefRepo.findAllByTeacherAndType(teacher, "semester"))
            .thenReturn(Arrays.asList(p1, p2));

        // act
        List<PreferenceDto> dtos = teacherService.getPreferences(42L, "semester");

        // assert
        assertEquals(2, dtos.size());

        PreferenceDto d1 = dtos.get(0);
        assertEquals(1L, d1.id);
        assertEquals("Math", d1.subject);
        assertEquals(Collections.singletonList("Mon"), d1.days.subList(0, 1));
        assertEquals("Tue", d1.days.get(1));
        assertEquals(10, d1.daysPriority);

        PreferenceDto d2 = dtos.get(1);
        assertEquals(2L, d2.id);
        assertTrue(d2.days.isEmpty());
        assertEquals(0, d2.daysPriority);

        verify(teacherRepo).findByUserId(42L);
        verify(prefRepo).findAllByTeacherAndType(teacher, "semester");
    }

    @Test
    void getPreferences_teacherNotFound_throws() {
        when(teacherRepo.findByUserId(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> teacherService.getPreferences(99L, "any")
        );
        assertEquals("Преподаватель не найден", ex.getMessage());
        verify(prefRepo, never()).findAllByTeacherAndType(any(), any());
    }

    @Test
    void savePreferences_emptyList_returnsEmpty() {
        // act
        List<PreferenceDto> result = teacherService.savePreferences(42L, Collections.emptyList());

        // assert
        assertTrue(result.isEmpty());
        verifyNoInteractions(teacherRepo, prefRepo);
    }

    @Test
    void savePreferences_success() {
        // arrange
        PreferenceDto dto = new PreferenceDto();
        dto.id = null;
        dto.type = "session";
        dto.subject = "Chemistry";
        dto.groups = "C3";
        dto.days = Arrays.asList("Wed", "Thu");
        dto.daysPriority = 7;
        dto.times = "14:00";
        dto.timesPriority = 4;
        dto.preferredDates = "2025-06-01";
        dto.avoidDates = "2025-06-05";
        dto.newYearPref = "Prefer none";
        dto.loadType = "Lecture";
        dto.loadTypePriority = 2;
        dto.buildingRoom = "B-101";
        dto.buildingRoomPriority = 1;
        dto.boardType = "Whiteboard";
        dto.boardTypePriority = 5;
        dto.computers = Arrays.asList("Lab1", "Lab2");
        dto.computersPriority = 3;
        dto.format = "Practical";
        dto.formatPriority = 6;
        dto.comments = "No comments";
        dto.commentsPriority = 0;

        when(teacherRepo.findByUserId(42L)).thenReturn(Optional.of(teacher));

        // stub delete
        doNothing().when(prefRepo).deleteAllByTeacherAndType(teacher, "session");

        // stub save to return a Preference with generated ID
        ArgumentCaptor<Preference> saveCaptor = ArgumentCaptor.forClass(Preference.class);
        when(prefRepo.save(saveCaptor.capture()))
            .thenAnswer(invocation -> {
                Preference p = invocation.getArgument(0);
                p.setId(99L);
                return p;
            });

        // act
        List<PreferenceDto> saved = teacherService.savePreferences(42L, List.of(dto));

        // assert
        assertEquals(1, saved.size());
        PreferenceDto out = saved.get(0);
        assertEquals(99L, out.id);
        assertEquals("Chemistry", out.subject);
        assertEquals(Arrays.asList("Wed", "Thu"), out.days);
        assertEquals(7, out.daysPriority);
        assertEquals("Lab1", out.computers.get(0));
        assertEquals("Lab2", out.computers.get(1));
        assertEquals(3, out.computersPriority);

        // verify deleteAll called once
        verify(prefRepo).deleteAllByTeacherAndType(teacher, "session");
        // verify save called once
        verify(prefRepo).save(any(Preference.class));

        // inspect the saved Preference entity
        Preference persisted = saveCaptor.getValue();
        assertEquals("session", persisted.getType());
        assertEquals("Chemistry", persisted.getSubject());
        assertEquals("Wed, Thu", persisted.getDays());
    }

    @Test
    void savePreferences_teacherNotFound_throws() {
        PreferenceDto dto = new PreferenceDto();
        dto.type = "semester";
        when(teacherRepo.findByUserId(42L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> teacherService.savePreferences(42L, List.of(dto))
        );
        assertEquals("Преподаватель не найден", ex.getMessage());
        verify(prefRepo, never()).deleteAllByTeacherAndType(any(), any());
    }
}
