package com.example.webapp.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import com.example.webapp.dto.PreferenceDto;
import com.example.webapp.model.Preference;
import com.example.webapp.repository.PreferenceRepository;

@Service
public class AdminService {

    @Autowired
    private PreferenceRepository prefRepo;

    /** Возвращает все DTO предпочтений */
    public List<PreferenceDto> getAllPreferences() {
        return prefRepo.findAll().stream()
                       .map(this::toDto)
                       .collect(Collectors.toList());
    }

    /** Экспорт в Excel с русскими заголовками и корректным выводом дней */
    public ByteArrayResource exportPreferencesToExcel() throws IOException {
        List<PreferenceDto> all = getAllPreferences().stream()
            .sorted(Comparator.comparing(d -> d.teacherName))
            .collect(Collectors.toList());

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Пожелания");

        // Заголовки таблицы
        String[] headers = {
            "Преподаватель", "Логин",
            "Тип", "Предмет", "Группы",
            "Нежелательные дни", "Время",
            "Предпочтительные даты", "Исключить даты", "Новогодние пожелания",
            "Нагрузка", "Корпус/аудитория", "Доска",
            "Компьютеры", "Формат", "Комментарии"
        };
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        String prevTeacher = null;
        int rowIdx = 1;
        for (PreferenceDto d : all) {
            Row row = sheet.createRow(rowIdx++);
            int c = 0;

            // 1) ФИО и логин — только при смене преподавателя
            if (!d.teacherName.equals(prevTeacher)) {
                row.createCell(c++).setCellValue(d.teacherName);
                row.createCell(c++).setCellValue(d.teacherLogin);
                prevTeacher = d.teacherName;
            } else {
                c += 2;
            }

            // 2) Тип (semester/session) → Русский
            row.createCell(c++).setCellValue(translateType(d.type));

            // 3) Предмет и группы
            row.createCell(c++).setCellValue(safeString(d.subject));
            row.createCell(c++).setCellValue(safeString(d.groups));

            // 4) Нежелательные дни (List<String>) + приоритет
            List<String> daysFiltered = d.days == null
                ?  Collections.emptyList()
                : d.days.stream()
                    .map(String::trim)     
                    .filter(s -> s.length() == 2)   // убираем пустые строки
                    .distinct()
                    .collect(Collectors.toList());

            String daysCell = String.join(", ", daysFiltered)
                 + (d.daysPriority != null 
                 ? " (" + d.daysPriority + ")" 
                 : "");
            row.createCell(c++).setCellValue(daysCell);

            // 5) Время + приоритет
            String timesCell = safeString(d.times)
                + (d.timesPriority != null ? " (" + d.timesPriority + ")" : "");
            row.createCell(c++).setCellValue(timesCell);

            // 6) Сессионные поля (без приоритетов)
            row.createCell(c++).setCellValue(safeString(d.preferredDates));
            row.createCell(c++).setCellValue(safeString(d.avoidDates));
            row.createCell(c++).setCellValue(safeString(d.newYearPref));

            // 7) Нагрузка + приоритет, переведённая
            String loadCell = translateLoadType(d.loadType)
                + (d.loadTypePriority != null ? " (" + d.loadTypePriority + ")" : "");
            row.createCell(c++).setCellValue(loadCell);

            // 8) Корпус/аудитория + приоритет
            String bldCell = safeString(d.buildingRoom)
                + (d.buildingRoomPriority != null ? " (" + d.buildingRoomPriority + ")" : "");
            row.createCell(c++).setCellValue(bldCell);

            // 9) Доска + приоритет, переведённая
            String boardCell = translateBoardType(d.boardType)
                + (d.boardTypePriority != null ? " (" + d.boardTypePriority + ")" : "");
            row.createCell(c++).setCellValue(boardCell);

            // 10) Компьютеры + приоритет
            String compList = d.computers == null
                ? ""
                : String.join(", ", d.computers);
            String compCell = compList
                + (d.computersPriority != null ? " (" + d.computersPriority + ")" : "");
            row.createCell(c++).setCellValue(compCell);

            // 11) Формат + приоритет, переведённый
            String fmtCell = translateFormat(d.format)
                + (d.formatPriority != null ? " (" + d.formatPriority + ")" : "");
            row.createCell(c++).setCellValue(fmtCell);

            // 12) Комментарии + приоритет
            String commCell = safeString(d.comments)
                + (d.commentsPriority != null ? " (" + d.commentsPriority + ")" : "");
            row.createCell(c++).setCellValue(commCell);
        }

        // Автоподгонка ширины столбцов
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Сериализация и возврат
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();
        return new ByteArrayResource(out.toByteArray());
    }

    // Вспомогательные методы

    private String safeString(String s) {
        return s == null ? "" : s;
    }

    private String translateType(String type) {
        switch (type) {
            case "semester": return "Семестр";
            case "session":  return "Сессия";
            default:         return safeString(type);
        }
    }

    private String translateLoadType(String lt) {
        switch (lt) {
            case "compact": return "Компактно";
            case "even":    return "Равномерно";
            default:        return safeString(lt);
        }
    }

    private String translateBoardType(String bt) {
        switch (bt) {
            case "marker":  return "Маркер";
            case "chalk":   return "Мел";
            case "digital": return "Цифровая";
            default:        return safeString(bt);
        }
    }

    private String translateFormat(String f) {
        switch (f) {
            case "in-person": return "Очно";
            case "remote":    return "Дистанционно";
            default:          return safeString(f);
        }
    }

    /** Переводим сущность в DTO */
    private PreferenceDto toDto(Preference p) {
        PreferenceDto d = new PreferenceDto();
        d.id                   = p.getId();
        d.type                 = p.getType();
        d.subject              = p.getSubject();
        d.groups               = p.getGroups();
        // Преобразуем строку days в список строк
        d.days = (p.getDays() == null || p.getDays().isBlank())
            ? List.of()
            : Arrays.asList(p.getDays().split("\\s*,\\s*"));
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
        d.computers            = (p.getComputers() == null || p.getComputers().isBlank())
                                  ? List.of()
                                  : Arrays.asList(p.getComputers().split("\\s*,\\s*"));
        d.computersPriority    = p.getComputersPriority();
        d.format               = p.getFormat();
        d.formatPriority       = p.getFormatPriority();
        d.comments             = p.getComments();
        d.commentsPriority     = p.getCommentsPriority();
        if (p.getTeacher() != null) {
            d.teacherName      = p.getTeacher().getName();
            d.teacherLogin     = p.getTeacher().getContactLogin();
        } else {
            d.teacherName      = "";
            d.teacherLogin     = "";
        }
        return d;
    }
}
