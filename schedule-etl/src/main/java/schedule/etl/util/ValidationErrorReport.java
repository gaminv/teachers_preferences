package schedule.etl.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import schedule.etl.model.SchedulePreferenceDto;
import schedule.etl.model.ValidationError;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import schedule.etl.model.ValidationError.Severity;

/**
 * Запись отчёта об ошибках валидации: TXT (legacy) или XLSX с оформлением и содержимым строк.
 * XLSX: сначала ошибки, потом предупреждения; красная заливка для ошибок, жёлтая для предупреждений; содержимое проблемной строки в тех же строках.
 */
public final class ValidationErrorReport {

    private ValidationErrorReport() {}

    private static final String HEADER = "Строка\tПоле\tТип\tОписание";
    private static final String SEP = "\t";

    /**
     * Записывает отчёт в Excel: сортировка (сначала ошибки, потом предупреждения), заливка строк (красный/жёлтый),
     * колонки с содержимым проблемной строки. Если {@code allRows == null}, колонки содержимого не заполняются.
     */
    public static Path writeToExcel(Path path, List<ValidationError> errors, List<SchedulePreferenceDto> allRows) throws IOException {
        if (errors == null || errors.isEmpty()) {
            return path;
        }
        List<ValidationError> sorted = errors.stream()
                .sorted(Comparator
                        .comparing(ValidationError::getSeverity, Comparator.comparingInt(s -> s == Severity.WARNING ? 1 : 0))
                        .thenComparing(ValidationError::getRowNumber))
                .toList();

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Ошибки и предупреждения");
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            CellStyle errorRowStyle = wb.createCellStyle();
            errorRowStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
            errorRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle warningRowStyle = wb.createCellStyle();
            warningRowStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            warningRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] contentHeaders = {
                    "Преподаватель", "Тип", "Дисциплина", "Группы",
                    "Нежелательные дни", "Время", "Нагрузка", "Корпус/аудитория", "Компьютеры", "Комментарии"
            };
            int contentCols = contentHeaders.length;
            String[] headerRow = new String[4 + contentCols];
            headerRow[0] = "Строка";
            headerRow[1] = "Поле";
            headerRow[2] = "Тип";
            headerRow[3] = "Описание";
            for (int i = 0; i < contentCols; i++) headerRow[4 + i] = contentHeaders[i];

            Row header = sheet.createRow(0);
            for (int i = 0; i < headerRow.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(headerRow[i]);
                c.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (ValidationError e : sorted) {
                Row row = sheet.createRow(rowIdx++);
                boolean isWarning = e.getSeverity() == Severity.WARNING;
                CellStyle rowStyle = isWarning ? warningRowStyle : errorRowStyle;

                row.createCell(0).setCellValue(e.getRowNumber());
                row.getCell(0).setCellStyle(rowStyle);
                row.createCell(1).setCellValue(e.getField() != null ? e.getField() : "");
                row.getCell(1).setCellStyle(rowStyle);
                row.createCell(2).setCellValue(isWarning ? "Предупреждение" : "Ошибка");
                row.getCell(2).setCellStyle(rowStyle);
                row.createCell(3).setCellValue(e.getMessage() != null ? e.getMessage() : "");
                row.getCell(3).setCellStyle(rowStyle);

                SchedulePreferenceDto dto = rowContentFor(allRows, e.getRowNumber());
                if (dto != null) {
                    row.createCell(4).setCellValue(safe(dto.getTeacherName()));
                    row.getCell(4).setCellStyle(rowStyle);
                    row.createCell(5).setCellValue(safe(dto.getType()));
                    row.getCell(5).setCellStyle(rowStyle);
                    row.createCell(6).setCellValue(safe(dto.getSubject()));
                    row.getCell(6).setCellStyle(rowStyle);
                    row.createCell(7).setCellValue(safe(dto.getGroups()));
                    row.getCell(7).setCellStyle(rowStyle);
                    row.createCell(8).setCellValue(dto.getDays() != null ? String.join(", ", dto.getDays()) : "");
                    row.getCell(8).setCellStyle(rowStyle);
                    row.createCell(9).setCellValue(safe(dto.getTimes()));
                    row.getCell(9).setCellStyle(rowStyle);
                    row.createCell(10).setCellValue(safe(dto.getLoadType()));
                    row.getCell(10).setCellStyle(rowStyle);
                    row.createCell(11).setCellValue(safe(dto.getBuildingRoom()));
                    row.getCell(11).setCellStyle(rowStyle);
                    row.createCell(12).setCellValue(dto.getComputers() != null ? String.join(", ", dto.getComputers()) : "");
                    row.getCell(12).setCellStyle(rowStyle);
                    row.createCell(13).setCellValue(safe(dto.getComments()));
                    row.getCell(13).setCellStyle(rowStyle);
                } else {
                    for (int i = 4; i < 4 + contentCols; i++) {
                        Cell c = row.createCell(i);
                        c.setCellStyle(rowStyle);
                    }
                }
            }

            for (int i = 0; i < headerRow.length; i++) {
                sheet.autoSizeColumn(i);
            }
            try (var out = Files.newOutputStream(path)) {
                wb.write(out);
            }
        }
        return path;
    }

    private static SchedulePreferenceDto rowContentFor(List<SchedulePreferenceDto> allRows, int excelRowNum) {
        if (allRows == null || allRows.isEmpty()) return null;
        int index = excelRowNum - 2; // в Excel строка 1 — заголовок, строка 2 — первая запись
        if (index < 0 || index >= allRows.size()) return null;
        return allRows.get(index);
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    /**
     * Записывает список ошибок и предупреждений в TXT (legacy). Для наглядного отчёта используйте {@link #writeToExcel}.
     */
    public static Path writeToFile(Path path, List<ValidationError> errors) throws IOException {
        if (errors == null || errors.isEmpty()) {
            return path;
        }
        List<String> lines = errors.stream()
                .map(e -> {
                    String type = e.getSeverity() == Severity.WARNING ? "Предупреждение" : "Ошибка";
                    String field = e.getField() != null ? e.getField() : "";
                    String msg = e.getMessage() != null ? e.getMessage() : "";
                    return e.getRowNumber() + SEP + field + SEP + type + SEP + msg;
                })
                .collect(Collectors.toList());
        lines.add(0, HEADER);
        Files.write(path, lines, StandardCharsets.UTF_8);
        return path;
    }

    /**
     * Формирует читаемый текст одной ошибки (для вывода в консоль или лог).
     */
    public static String formatLine(ValidationError e) {
        return e.toString();
    }
}
