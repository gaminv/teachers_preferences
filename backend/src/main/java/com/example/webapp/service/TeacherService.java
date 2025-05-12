package com.example.webapp.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.webapp.dto.PreferenceDto;
import com.example.webapp.model.Preference;
import com.example.webapp.model.Teacher;
import com.example.webapp.repository.PreferenceRepository;
import com.example.webapp.repository.TeacherRepository;

@Service
public class TeacherService {

    @Autowired
    private TeacherRepository teacherRepo;

    @Autowired
    private PreferenceRepository prefRepo;

    /**
     * Получение всех предпочтений данного преподавателя по типу.
     */
    public List<PreferenceDto> getPreferences(Long userId, String type) {
        Teacher teacher = teacherRepo.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));
        return prefRepo.findAllByTeacherAndType(teacher, type).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Сохранение (полная перезапись) списка предпочтений данного преподавателя.
     */
    @Transactional
    public List<PreferenceDto> savePreferences(Long userId, List<PreferenceDto> dtos) {
        if (dtos.isEmpty()) {
            return Collections.emptyList();
        }
        String type = dtos.get(0).type;
        Teacher teacher = teacherRepo.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Преподаватель не найден"));

        // Удаляем все старые записи этого типа
        prefRepo.deleteAllByTeacherAndType(teacher, type);

        // Сохраняем новые
        List<Preference> saved = dtos.stream().map(dto -> {
            Preference p = new Preference();
            p.setTeacher(teacher);
            p.setType(dto.type);
            p.setSubject(dto.subject);
            p.setGroups(dto.groups);

            // семестровые поля + приоритеты: List<String> → CSV
            String daysCsv = (dto.days != null && !dto.days.isEmpty())
                ? String.join(", ", dto.days)
                : "";
            p.setDays(daysCsv);
            p.setDaysPriority(dto.daysPriority);

            p.setTimes(dto.times);
            p.setTimesPriority(dto.timesPriority);

            // сессионные поля
            p.setPreferredDates(dto.preferredDates);
            p.setAvoidDates(dto.avoidDates);
            p.setNewYearPref(dto.newYearPref);

            // общие поля + приоритеты
            p.setLoadType(dto.loadType);
            p.setLoadTypePriority(dto.loadTypePriority);
            p.setBuildingRoom(dto.buildingRoom);
            p.setBuildingRoomPriority(dto.buildingRoomPriority);
            p.setBoardType(dto.boardType);
            p.setBoardTypePriority(dto.boardTypePriority);

            // компьютеры (List<String>) → CSV + приоритет
            String compCsv = (dto.computers != null && !dto.computers.isEmpty())
                ? String.join(", ", dto.computers)
                : "";
            p.setComputers(compCsv);
            p.setComputersPriority(dto.computersPriority);

            p.setFormat(dto.format);
            p.setFormatPriority(dto.formatPriority);

            p.setComments(dto.comments);
            p.setCommentsPriority(dto.commentsPriority);

            return prefRepo.save(p);
        }).collect(Collectors.toList());

        return saved.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }


    private PreferenceDto toDto(Preference p) {
        PreferenceDto d = new PreferenceDto();
        d.id                   = p.getId();
        d.type                 = p.getType();
        d.subject              = p.getSubject();
        d.groups               = p.getGroups();


        if (p.getDays() == null || p.getDays().isBlank()) {
            d.days = Collections.emptyList();
        } else {
            d.days = Arrays.asList(p.getDays().split("\\s*,\\s*"));
        }
        d.daysPriority         = p.getDaysPriority();

        d.times                = p.getTimes();
        d.timesPriority        = p.getTimesPriority();

        d.preferredDates       = p.getPreferredDates();
        d.avoidDates           = p.getAvoidDates();
        d.newYearPref          = p.getNewYearPref();

        d.loadType             = p.getLoadType();
        d.loadTypePriority     = p.getLoadTypePriority();
        d.buildingRoom         = p.getBuildingRoom();
        d.buildingRoomPriority = p.getBuildingRoomPriority();
        d.boardType            = p.getBoardType();
        d.boardTypePriority    = p.getBoardTypePriority();

  
        if (p.getComputers() == null || p.getComputers().isBlank()) {
            d.computers = Collections.emptyList();
        } else {
            d.computers = Arrays.asList(p.getComputers().split("\\s*,\\s*"));
        }
        d.computersPriority    = p.getComputersPriority();

        d.format               = p.getFormat();
        d.formatPriority       = p.getFormatPriority();

        d.comments             = p.getComments();
        d.commentsPriority     = p.getCommentsPriority();

        return d;
    }
}
