package schedule.etl.write;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import schedule.etl.api.ScheduleExcelWriter;
import schedule.etl.config.ExcelTemplateConstants;
import schedule.etl.model.SchedulePreferenceDto;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Запись пожеланий в XLSX по шаблону сайта (лист «Пожелания», те же столбцы, что и при экспорте).
 */
public final class PoiScheduleExcelWriter implements ScheduleExcelWriter {

    @Override
    public void write(List<SchedulePreferenceDto> data, OutputStream output) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet(ExcelTemplateConstants.SHEET_NAME);
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < ExcelTemplateConstants.HEADERS.length; i++) {
                headerRow.createCell(i).setCellValue(ExcelTemplateConstants.HEADERS[i]);
            }
            int rowIdx = 1;
            for (SchedulePreferenceDto d : data) {
                Row row = sheet.createRow(rowIdx++);
                int c = 0;
                row.createCell(c++).setCellValue(safe(d.getTeacherName()));
                row.createCell(c++).setCellValue(safe(d.getTeacherLogin()));
                row.createCell(c++).setCellValue(typeToRu(d.getType()));
                row.createCell(c++).setCellValue(safe(d.getSubject()));
                row.createCell(c++).setCellValue(safe(d.getGroups()));
                String daysStr = d.getDays() != null ? String.join(", ", d.getDays()) : "";
                row.createCell(c++).setCellValue(withPriority(daysStr, d.getDaysPriority()));
                row.createCell(c++).setCellValue(withPriority(d.getTimes(), d.getTimesPriority()));
                row.createCell(c++).setCellValue(safe(d.getPreferredDates()));
                row.createCell(c++).setCellValue(safe(d.getAvoidDates()));
                row.createCell(c++).setCellValue(safe(d.getNewYearPref()));
                row.createCell(c++).setCellValue(withPriority(loadTypeToRu(d.getLoadType()), d.getLoadTypePriority()));
                row.createCell(c++).setCellValue(withPriority(d.getBuildingRoom(), d.getBuildingRoomPriority()));
                row.createCell(c++).setCellValue(withPriority(boardTypeToRu(d.getBoardType()), d.getBoardTypePriority()));
                String compStr = d.getComputers() != null ? String.join(", ", d.getComputers()) : "";
                row.createCell(c++).setCellValue(withPriority(compStr, d.getComputersPriority()));
                row.createCell(c++).setCellValue(withPriority(formatToRu(d.getFormat()), d.getFormatPriority()));
                row.createCell(c++).setCellValue(withPriority(d.getComments(), d.getCommentsPriority()));
            }
            for (int i = 0; i < ExcelTemplateConstants.HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }
            wb.write(output);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static String typeToRu(String type) {
        if (type == null) return "";
        return switch (type) {
            case "semester" -> "Семестр";
            case "session" -> "Сессия";
            default -> type;
        };
    }

    private static String loadTypeToRu(String lt) {
        if (lt == null) return "";
        return switch (lt) {
            case "compact" -> "Компактно";
            case "even" -> "Равномерно";
            default -> lt;
        };
    }

    private static String boardTypeToRu(String bt) {
        if (bt == null) return "";
        return switch (bt) {
            case "marker" -> "Маркер";
            case "chalk" -> "Мел";
            case "digital" -> "Цифровая";
            default -> bt;
        };
    }

    private static String formatToRu(String f) {
        if (f == null) return "";
        return switch (f) {
            case "in-person" -> "Очно";
            case "remote" -> "Дистанционно";
            default -> f;
        };
    }

    private static String withPriority(String value, Integer priority) {
        String v = safe(value);
        if (priority != null && !v.isEmpty()) return v + " (" + priority + ")";
        return v;
    }
}
