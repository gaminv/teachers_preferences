package schedule.etl.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import schedule.etl.ScheduleEtlFactory;
import schedule.etl.ScheduleEtlService;
import schedule.etl.model.EtlResult;
import schedule.etl.model.SchedulePreferenceDto;
import schedule.etl.util.SampleDataGenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class ScheduleEtlIntegrationTest {

    private final ScheduleEtlService service = ScheduleEtlFactory.createService();

    @Test
    void scenario01_validData_ImportWorks() throws Exception {
        var data = SampleDataGenerator.generateMinimal(5);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.writeToExcel(data, out);
        EtlResult result = service.excelToResult(new ByteArrayInputStream(out.toByteArray()));
        assertTrue(result.isSuccess());
        assertEquals(5, result.getData().size());
    }

    @Test
    void scenario02_errorsAreExcludedFromData() throws Exception {
        var data = SampleDataGenerator.generateWithErrors(20, 4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.writeToExcel(data, out);
        EtlResult result = service.excelToResult(new ByteArrayInputStream(out.toByteArray()));
        assertFalse(result.getValidationErrors().isEmpty());
        assertTrue(result.getData().size() < 20);
    }

    @Test
    void scenario03_warningsStayInData() throws Exception {
        var data = SampleDataGenerator.generateWithWarnings(20, 3);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.writeToExcel(data, out);
        EtlResult result = service.excelToResult(new ByteArrayInputStream(out.toByteArray()));
        long warningCount = result.getValidationErrors().stream().filter(e -> e.getSeverity().name().equals("WARNING")).count();
        assertTrue(warningCount >= 3);
        assertEquals(20, result.getData().size());
    }

    @Test
    void scenario04_excelToJsonFailsForErrorCases() throws Exception {
        var data = SampleDataGenerator.generateWithErrors(10, 2);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.writeToExcel(data, out);
        assertThrows(Exception.class, () -> service.excelToJson(new ByteArrayInputStream(out.toByteArray())));
    }

    @Test
    void scenario05_jsonExportAndImportKeepsRowCount() throws Exception {
        var data = SampleDataGenerator.generateMinimal(12);
        String json = service.toJson(data);
        byte[] excel = service.jsonToExcelBytes(json);
        EtlResult result = service.excelToResult(new ByteArrayInputStream(excel));
        assertEquals(12, result.getData().size());
    }

    @Test
    void scenario06_typeMappingRuToInternalWorks() throws Exception {
        SchedulePreferenceDto dto = new SchedulePreferenceDto();
        dto.setTeacherName("Иванов И. И.");
        dto.setType("session");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.writeToExcel(List.of(dto), out);
        EtlResult result = service.excelToResult(new ByteArrayInputStream(out.toByteArray()));
        assertEquals("session", result.getData().get(0).getType());
    }

    @Test
    void scenario07_prioritiesRoundtripWorks() throws Exception {
        SchedulePreferenceDto dto = new SchedulePreferenceDto();
        dto.setTeacherName("Иванов И. И.");
        dto.setType("semester");
        dto.setDays(List.of("Пн", "Вт"));
        dto.setDaysPriority(5);
        dto.setTimes("до 14:00");
        dto.setTimesPriority(1);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.writeToExcel(List.of(dto), out);
        EtlResult result = service.excelToResult(new ByteArrayInputStream(out.toByteArray()));
        assertEquals(5, result.getData().get(0).getDaysPriority());
        assertEquals(1, result.getData().get(0).getTimesPriority());
    }

    @Test
    void scenario08_computersRoundtripWorks() throws Exception {
        SchedulePreferenceDto dto = new SchedulePreferenceDto();
        dto.setTeacherName("Иванов И. И.");
        dto.setType("semester");
        dto.setComputers(List.of("Windows", "Linux"));
        dto.setComputersPriority(2);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.writeToExcel(List.of(dto), out);
        EtlResult result = service.excelToResult(new ByteArrayInputStream(out.toByteArray()));
        assertEquals(List.of("Windows", "Linux"), result.getData().get(0).getComputers());
    }

    @Test
    void scenario09_emptyDatasetIsSupported() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.writeToExcel(List.of(), out);
        EtlResult result = service.excelToResult(new ByteArrayInputStream(out.toByteArray()));
        assertTrue(result.isSuccess());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    void scenario10_largeDatasetIsProcessed() throws Exception {
        var data = SampleDataGenerator.generateMinimal(200);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.writeToExcel(data, out);
        EtlResult result = service.excelToResult(new ByteArrayInputStream(out.toByteArray()));
        assertEquals(200, result.getData().size());
    }
}
