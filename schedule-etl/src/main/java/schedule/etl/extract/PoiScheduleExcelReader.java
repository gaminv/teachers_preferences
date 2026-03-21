package schedule.etl.extract;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import schedule.etl.api.ScheduleExcelReader;
import schedule.etl.config.ExcelTemplateConstants;
import schedule.etl.model.SchedulePreferenceDto;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extract: чтение шаблона Excel (лист «Пожелания») через Apache POI.
 * Поддержка XLS/XLSX; ячейки с формулами обрабатываются через кэшированное значение (DataFormatter).
 */
public final class PoiScheduleExcelReader implements ScheduleExcelReader {

    private static final Pattern PRIORITY_SUFFIX = Pattern.compile("^(.+?)\\s*\\((\\d+)\\)\\s*$");

    @Override
    public List<SchedulePreferenceDto> read(InputStream excelInput) throws IOException {
        try (Workbook wb = WorkbookFactory.create(excelInput)) {
            Sheet sheet = wb.getSheet(ExcelTemplateConstants.SHEET_NAME);
            if (sheet == null) {
                sheet = wb.getSheetAt(0);
            }
            if (sheet == null) {
                return Collections.emptyList();
            }
            validateStructure(sheet);
            DataFormatter formatter = new DataFormatter();
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            List<SchedulePreferenceDto> result = new ArrayList<>();
            String lastTeacher = "";
            String lastLogin = "";
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String teacher = cellStr(row, ExcelTemplateConstants.COL_TEACHER, formatter, evaluator);
                String login = cellStr(row, ExcelTemplateConstants.COL_LOGIN, formatter, evaluator);
                if (!teacher.isEmpty()) lastTeacher = teacher;
                if (!login.isEmpty()) lastLogin = login;
                if (lastTeacher.isEmpty()) continue;

                SchedulePreferenceDto dto = new SchedulePreferenceDto();
                dto.setTeacherName(lastTeacher);
                dto.setTeacherLogin(lastLogin);
                dto.setType(typeFromRu(cellStr(row, ExcelTemplateConstants.COL_TYPE, formatter, evaluator)));
                dto.setSubject(cellStr(row, ExcelTemplateConstants.COL_SUBJECT, formatter, evaluator));
                dto.setGroups(cellStr(row, ExcelTemplateConstants.COL_GROUPS, formatter, evaluator));

                var days = parseListWithPriority(cellStr(row, ExcelTemplateConstants.COL_DAYS, formatter, evaluator));
                dto.setDays(days.items().isEmpty() ? null : days.items());
                dto.setDaysPriority(days.priority());

                var times = parseValueWithPriority(cellStr(row, ExcelTemplateConstants.COL_TIMES, formatter, evaluator));
                dto.setTimes(times.value().isEmpty() ? null : times.value());
                dto.setTimesPriority(times.priority());

                dto.setPreferredDates(emptyToNull(cellStr(row, ExcelTemplateConstants.COL_PREF_DATES, formatter, evaluator)));
                dto.setAvoidDates(emptyToNull(cellStr(row, ExcelTemplateConstants.COL_AVOID_DATES, formatter, evaluator)));
                dto.setNewYearPref(emptyToNull(cellStr(row, ExcelTemplateConstants.COL_NEW_YEAR, formatter, evaluator)));

                var load = parseValueWithPriority(cellStr(row, ExcelTemplateConstants.COL_LOAD, formatter, evaluator));
                dto.setLoadType(loadTypeFromRu(load.value()));
                dto.setLoadTypePriority(load.priority());

                var bld = parseValueWithPriority(cellStr(row, ExcelTemplateConstants.COL_BUILDING, formatter, evaluator));
                dto.setBuildingRoom(emptyToNull(bld.value()));
                dto.setBuildingRoomPriority(bld.priority());

                var board = parseValueWithPriority(cellStr(row, ExcelTemplateConstants.COL_BOARD, formatter, evaluator));
                dto.setBoardType(boardTypeFromRu(board.value()));
                dto.setBoardTypePriority(board.priority());

                var comp = parseListWithPriority(cellStr(row, ExcelTemplateConstants.COL_COMPUTERS, formatter, evaluator));
                dto.setComputers(comp.items().isEmpty() ? null : comp.items());
                dto.setComputersPriority(comp.priority());

                var fmt = parseValueWithPriority(cellStr(row, ExcelTemplateConstants.COL_FORMAT, formatter, evaluator));
                dto.setFormat(formatFromRu(fmt.value()));
                dto.setFormatPriority(fmt.priority());

                var comm = parseValueWithPriority(cellStr(row, ExcelTemplateConstants.COL_COMMENTS, formatter, evaluator));
                dto.setComments(emptyToNull(comm.value()));
                dto.setCommentsPriority(comm.priority());

                result.add(dto);
            }
            return result;
        }
    }

    private static String cellStr(Row row, int col, DataFormatter formatter, FormulaEvaluator evaluator) {
        Cell c = row.getCell(col);
        if (c == null) return "";
        try {
            String value = formatter.formatCellValue(c, evaluator);
            return value != null ? value.trim() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private static String typeFromRu(String ru) {
        if (ru == null) return "semester";
        return switch (ru.trim()) {
            case "Семестр" -> "semester";
            case "Сессия" -> "session";
            default -> ru.trim().isEmpty() ? "semester" : ru.trim();
        };
    }

    private static String loadTypeFromRu(String ru) {
        if (ru == null) return null;
        return switch (ru.trim()) {
            case "Компактно" -> "compact";
            case "Равномерно" -> "even";
            default -> ru.trim().isEmpty() ? null : ru.trim();
        };
    }

    private static String boardTypeFromRu(String ru) {
        if (ru == null) return null;
        return switch (ru.trim()) {
            case "Маркер" -> "marker";
            case "Мел" -> "chalk";
            case "Цифровая" -> "digital";
            default -> ru.trim().isEmpty() ? null : ru.trim();
        };
    }

    private static String formatFromRu(String ru) {
        if (ru == null) return null;
        return switch (ru.trim()) {
            case "Очно" -> "in-person";
            case "Дистанционно" -> "remote";
            default -> ru.trim().isEmpty() ? null : ru.trim();
        };
    }

    private static ValueWithPriority parseValueWithPriority(String s) {
        if (s == null || s.isBlank()) return new ValueWithPriority("", null);
        Matcher m = PRIORITY_SUFFIX.matcher(s.trim());
        if (m.matches()) {
            return new ValueWithPriority(m.group(1).trim(), Integer.valueOf(m.group(2)));
        }
        return new ValueWithPriority(s.trim(), null);
    }

    private static ListWithPriority parseListWithPriority(String s) {
        ValueWithPriority vp = parseValueWithPriority(s);
        if (vp.value().isBlank()) return new ListWithPriority(List.of(), null);
        List<String> items = Arrays.stream(vp.value().split(","))
                .map(String::trim)
                .filter(x -> !x.isEmpty())
                .toList();
        return new ListWithPriority(items, vp.priority());
    }

    private static String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }

    /** Структурная валидация: первая строка должна содержать ожидаемые заголовки. */
    private static void validateStructure(Sheet sheet) throws IOException {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IOException("Структура шаблона нарушена: отсутствует строка заголовков (первая строка).");
        }
        DataFormatter df = new DataFormatter();
        Cell firstCell = headerRow.getCell(ExcelTemplateConstants.COL_TEACHER);
        String firstHeader = firstCell != null ? df.formatCellValue(firstCell).trim() : "";
        if (!firstHeader.contains("Преподаватель")) {
            throw new IOException("Структура шаблона нарушена: в первой строке ожидается столбец «Преподаватель». Убедитесь, что используется шаблон пожеланий с листом «Пожелания».");
        }
    }

    private record ValueWithPriority(String value, Integer priority) {}
    private record ListWithPriority(List<String> items, Integer priority) {}
}
