package schedule.etl;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import schedule.etl.model.EtlResult;
import schedule.etl.model.SchedulePreferenceDto;
import schedule.etl.model.ValidationError;

import schedule.etl.util.ValidationErrorReport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class ScheduleEtlServiceTest {

    private final ScheduleEtlService service = ScheduleEtlFactory.createService();

    @Test
    void roundtrip_jsonToExcelToJson_preservesData() throws Exception {
        String json = """
                [
                  {"teacherName": "Тестов Тест Тестович", "teacherLogin": "test", "type": "semester", "subject": "Математика", "groups": "Гр-1", "days": ["Пн"], "daysPriority": 5}
                ]
                """;
        List<SchedulePreferenceDto> original = service.fromJson(json);
        assertEquals(1, original.size());
        assertEquals("Тестов Тест Тестович", original.get(0).getTeacherName());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.writeToExcel(original, out);
        byte[] excel = out.toByteArray();
        assertTrue(excel.length > 0);

        EtlResult result = service.excelToResult(new ByteArrayInputStream(excel));
        assertTrue(result.isSuccess(), "Validation errors: " + result.getErrors());
        assertEquals(1, result.getData().size());
        assertEquals("Тестов Тест Тестович", result.getData().get(0).getTeacherName());
        assertEquals("semester", result.getData().get(0).getType());
    }

    @Test
    void roundtrip_excelToJsonToExcel_preservesAllFields() throws Exception {
        List<SchedulePreferenceDto> original = schedule.etl.util.SampleDataGenerator.generateMinimal(3);
        original.get(0).setPreferredDates("до 20 января");
        original.get(0).setDays(List.of("Пн", "Вт"));
        original.get(0).setDaysPriority(3);
        original.get(1).setType("session");
        original.get(1).setNewYearPref("без занятий 2–3 января");
        ByteArrayOutputStream excel1 = new ByteArrayOutputStream();
        service.writeToExcel(original, excel1);
        EtlResult result = service.excelToResult(new ByteArrayInputStream(excel1.toByteArray()));
        assertTrue(result.isSuccess());
        assertEquals(3, result.getData().size());
        String json = service.toJson(result.getData());
        ByteArrayOutputStream excel2 = new ByteArrayOutputStream();
        service.writeToExcel(service.fromJson(json), excel2);
        EtlResult result2 = service.excelToResult(new ByteArrayInputStream(excel2.toByteArray()));
        assertEquals(3, result2.getData().size());
        assertEquals(original.get(0).getTeacherName(), result2.getData().get(0).getTeacherName());
        assertEquals(3, result2.getData().get(0).getDaysPriority());
        assertEquals("session", result2.getData().get(1).getType());
    }

    @Test
    void toJson_fromJson_roundtrip() throws Exception {
        SchedulePreferenceDto dto = new SchedulePreferenceDto();
        dto.setTeacherName("А Б В");
        dto.setTeacherLogin("abv");
        dto.setType("semester");
        String json = service.toJson(List.of(dto));
        List<SchedulePreferenceDto> restored = service.fromJson(json);
        assertEquals(1, restored.size());
        assertEquals("А Б В", restored.get(0).getTeacherName());
    }

    @Test
    void validation_teacherName_one_word_adds_warning_row_stays_in_data() throws Exception {
        SchedulePreferenceDto oneWord = new SchedulePreferenceDto();
        oneWord.setTeacherName("Иванов");
        oneWord.setTeacherLogin("ivanov");
        oneWord.setType("semester");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.writeToExcel(List.of(oneWord), out);
        EtlResult result = service.excelToResult(new ByteArrayInputStream(out.toByteArray()));
        assertFalse(result.getValidationErrors().isEmpty(), "Expected warning for one-word name");
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("ФИО") || e.contains("пробел") || e.contains("Рекомендуется")),
                "Expected warning about teacher name format: " + result.getErrors());
        assertEquals(1, result.getData().size(), "Row with one-word name stays in data (warning only)");
        assertEquals("Иванов", result.getData().get(0).getTeacherName());
    }

    @Test
    void validationErrors_have_row_field_and_message() throws Exception {
        SchedulePreferenceDto oneWord = new SchedulePreferenceDto();
        oneWord.setTeacherName("БезПробела");
        oneWord.setType("semester");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.writeToExcel(List.of(oneWord), out);
        EtlResult result = service.excelToResult(new ByteArrayInputStream(out.toByteArray()));
        List<ValidationError> errs = result.getValidationErrors();
        assertFalse(errs.isEmpty());
        assertEquals(2, errs.get(0).getRowNumber());
        assertNotNull(errs.get(0).getField());
        assertNotNull(errs.get(0).getMessage());
    }

    @Test
    void excelToJson_throws_when_there_are_errors() throws Exception {
        SchedulePreferenceDto invalid = new SchedulePreferenceDto();
        invalid.setTeacherName("Один Два"); // valid
        invalid.setType("year"); // invalid type — ERROR
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.writeToExcel(List.of(invalid), out);
        assertThrows(IOException.class, () ->
                service.excelToJson(new ByteArrayInputStream(out.toByteArray())));
    }

    @Test
    void import_returns_json_with_valid_rows_even_when_some_rows_have_errors() throws Exception {
        SchedulePreferenceDto valid = new SchedulePreferenceDto();
        valid.setTeacherName("Иванов И. И.");
        valid.setType("semester");
        SchedulePreferenceDto invalidType = new SchedulePreferenceDto();
        invalidType.setTeacherName("Петров П. П.");
        invalidType.setType("year");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.writeToExcel(List.of(valid, invalidType), out);
        EtlResult result = service.excelToResult(new ByteArrayInputStream(out.toByteArray()));
        assertEquals(1, result.getData().size(), "Only valid row in data");
        assertEquals("Иванов И. И.", result.getData().get(0).getTeacherName());
        assertFalse(result.getValidationErrors().isEmpty());
        assertTrue(result.getValidationErrors().stream().anyMatch(e -> e.getMessage().contains("Семестр") || e.getMessage().contains("Сессия")));
    }

    @Test
    void empty_excel_returns_empty_list_and_success() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.writeToExcel(List.of(), out);
        EtlResult result = service.excelToResult(new ByteArrayInputStream(out.toByteArray()));
        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    void jsonToExcelBytes_roundtrip() throws Exception {
        String json = "[{\"teacherName\":\"А Б\",\"teacherLogin\":\"ab\",\"type\":\"semester\"}]";
        byte[] excel = service.jsonToExcelBytes(json);
        assertTrue(excel.length > 0);
        EtlResult result = service.excelToResult(new ByteArrayInputStream(excel));
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertEquals("А Б", result.getData().get(0).getTeacherName());
    }

    @Test
    void writeValidationErrorsToFile_createsFileWithRowAndMessage() throws Exception {
        SchedulePreferenceDto oneWord = new SchedulePreferenceDto();
        oneWord.setTeacherName("БезПробела");
        oneWord.setType("semester");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.writeToExcel(List.of(oneWord), out);
        EtlResult result = service.excelToResult(new ByteArrayInputStream(out.toByteArray()));
        assertFalse(result.getValidationErrors().isEmpty());
        Path errFile = Files.createTempFile("etl-errors-", ".xlsx");
        try {
            ValidationErrorReport.writeToExcel(errFile, result.getValidationErrors(), result.getAllRows());
            try (var wb = WorkbookFactory.create(Files.newInputStream(errFile))) {
                Sheet sheet = wb.getSheetAt(0);
                assertNotNull(sheet);
                DataFormatter df = new DataFormatter();
                String header = df.formatCellValue(sheet.getRow(0).getCell(0));
                assertTrue(header.contains("Строка"));
                String row1Type = sheet.getRow(1) != null ? df.formatCellValue(sheet.getRow(1).getCell(2)) : "";
                assertTrue(row1Type.contains("Предупреждение") || row1Type.contains("Ошибка"));
            }
        } finally {
            Files.deleteIfExists(errFile);
        }
    }

    @Test
    void service_writeValidationErrorsToFile_returnsPathWhenErrors() throws Exception {
        SchedulePreferenceDto oneWord = new SchedulePreferenceDto();
        oneWord.setTeacherName("X");
        oneWord.setType("semester");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.writeToExcel(List.of(oneWord), out);
        EtlResult result = service.excelToResult(new ByteArrayInputStream(out.toByteArray()));
        Path errFile = Files.createTempFile("etl-svc-errors-", ".xlsx");
        try {
            Path written = service.writeValidationErrorsToFile(errFile, result);
            assertNotNull(written);
            assertTrue(Files.size(written) > 0);
        } finally {
            Files.deleteIfExists(errFile);
        }
    }
}
