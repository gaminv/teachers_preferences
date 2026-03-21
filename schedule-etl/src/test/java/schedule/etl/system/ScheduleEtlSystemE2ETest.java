package schedule.etl.system;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import schedule.etl.ScheduleEtlFactory;
import schedule.etl.ScheduleEtlService;
import schedule.etl.model.EtlResult;
import schedule.etl.model.SchedulePreferenceDto;
import schedule.etl.util.SampleDataGenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("system")
class ScheduleEtlSystemE2ETest {

    private final ScheduleEtlService service = ScheduleEtlFactory.createService();

    @Test
    void e2e01_generateTemplateLikeData_importAndExport() throws Exception {
        List<SchedulePreferenceDto> generated = SampleDataGenerator.generateWithErrors(100, 6);
        ByteArrayOutputStream excelOut = new ByteArrayOutputStream();
        service.writeToExcel(generated, excelOut);
        EtlResult result = service.excelToResult(new ByteArrayInputStream(excelOut.toByteArray()));
        String json = service.toJson(result.getData());
        byte[] exported = service.jsonToExcelBytes(json);
        assertTrue(exported.length > 0);
    }

    @Test
    void e2e02_warningReportCanBeGeneratedToFile() throws Exception {
        var generated = SampleDataGenerator.generateWithWarnings(20, 5);
        ByteArrayOutputStream excelOut = new ByteArrayOutputStream();
        service.writeToExcel(generated, excelOut);
        EtlResult result = service.excelToResult(new ByteArrayInputStream(excelOut.toByteArray()));
        Path tmp = Files.createTempFile("e2e-report-", ".xlsx");
        try {
            Path written = service.writeValidationErrorsToFile(tmp, result);
            assertNotNull(written);
            assertTrue(Files.size(written) > 0);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    void e2e03_invalidRowsExcluded() throws Exception {
        var generated = SampleDataGenerator.generateWithErrors(50, 10);
        ByteArrayOutputStream excelOut = new ByteArrayOutputStream();
        service.writeToExcel(generated, excelOut);
        EtlResult result = service.excelToResult(new ByteArrayInputStream(excelOut.toByteArray()));
        assertTrue(result.getData().size() <= 40);
    }

    @Test
    void e2e04_cleanDataNoErrors() throws Exception {
        var generated = SampleDataGenerator.generate(30);
        ByteArrayOutputStream excelOut = new ByteArrayOutputStream();
        service.writeToExcel(generated, excelOut);
        EtlResult result = service.excelToResult(new ByteArrayInputStream(excelOut.toByteArray()));
        assertTrue(result.isSuccess());
    }

    @Test
    void e2e05_jsonCompatibilityForBusinessKernel() throws Exception {
        var generated = SampleDataGenerator.generateMinimal(8);
        String json = service.toJson(generated);
        assertTrue(json.contains("teacherName"));
        var restored = service.fromJson(json);
        assertEquals(8, restored.size());
    }

    @Test
    void e2e06_filesystemRoundtripThroughTempFiles() throws Exception {
        Path jsonFile = Files.createTempFile("etl-", ".json");
        Path xlsxFile = Files.createTempFile("etl-", ".xlsx");
        try {
            var generated = SampleDataGenerator.generateMinimal(6);
            Files.writeString(jsonFile, service.toJson(generated));
            byte[] excel = service.jsonToExcelBytes(Files.readString(jsonFile));
            Files.write(xlsxFile, excel);
            EtlResult result = service.excelToResult(Files.newInputStream(xlsxFile));
            assertEquals(6, result.getData().size());
        } finally {
            Files.deleteIfExists(jsonFile);
            Files.deleteIfExists(xlsxFile);
        }
    }

    @Test
    void e2e07_longCommentsDontBreakPipeline() throws Exception {
        SchedulePreferenceDto dto = new SchedulePreferenceDto();
        dto.setTeacherName("Иванов И. И.");
        dto.setType("semester");
        dto.setComments("x".repeat(1000));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.writeToExcel(List.of(dto), out);
        EtlResult result = service.excelToResult(new ByteArrayInputStream(out.toByteArray()));
        assertTrue(result.isSuccess());
    }

    @Test
    void e2e08_warningOnlyCaseStillSerializable() throws Exception {
        SchedulePreferenceDto dto = new SchedulePreferenceDto();
        dto.setTeacherName("Иванов");
        dto.setType("semester");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.writeToExcel(List.of(dto), out);
        EtlResult result = service.excelToResult(new ByteArrayInputStream(out.toByteArray()));
        String json = service.toJson(result.getData());
        assertTrue(json.contains("Иванов"));
    }

    @Test
    void e2e09_multipleConversionsStable() throws Exception {
        var generated = SampleDataGenerator.generateMinimal(15);
        String json1 = service.toJson(generated);
        byte[] excel1 = service.jsonToExcelBytes(json1);
        EtlResult result = service.excelToResult(new ByteArrayInputStream(excel1));
        String json2 = service.toJson(result.getData());
        assertEquals(service.fromJson(json1).size(), service.fromJson(json2).size());
    }

    @Test
    void e2e10_performanceSmoke_300Rows() throws Exception {
        var generated = SampleDataGenerator.generateMinimal(300);
        long start = System.currentTimeMillis();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        service.writeToExcel(generated, out);
        EtlResult result = service.excelToResult(new ByteArrayInputStream(out.toByteArray()));
        long elapsed = System.currentTimeMillis() - start;
        assertEquals(300, result.getData().size());
        assertTrue(elapsed < 10_000);
    }
}
