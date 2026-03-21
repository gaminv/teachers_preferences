package schedule.etl.unit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import schedule.etl.ScheduleEtlService;
import schedule.etl.api.ScheduleExcelReader;
import schedule.etl.api.ScheduleExcelWriter;
import schedule.etl.model.SchedulePreferenceDto;
import schedule.etl.model.ValidationError;
import schedule.etl.transform.ScheduleTransform;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
class ScheduleEtlServiceUnitMockTest {

    private SchedulePreferenceDto dto(String teacher) {
        SchedulePreferenceDto dto = new SchedulePreferenceDto();
        dto.setTeacherName(teacher);
        dto.setType("semester");
        return dto;
    }

    @Test
    void excelToResultFiltersErrorRowsAndKeepsWarningRows() throws Exception {
        ScheduleExcelReader reader = mock(ScheduleExcelReader.class);
        ScheduleTransform transform = mock(ScheduleTransform.class);
        ScheduleExcelWriter writer = mock(ScheduleExcelWriter.class);
        var s = new ScheduleEtlService(reader, transform, writer);
        var rows = List.of(dto("A A"), dto("B B"), dto("C C"));
        when(reader.read(any())).thenReturn(rows);
        when(transform.validate(rows)).thenReturn(List.of(
                new ValidationError(3, "type", "bad type", ValidationError.Severity.ERROR),
                new ValidationError(4, "name", "warning", ValidationError.Severity.WARNING)
        ));
        var result = s.excelToResult(new ByteArrayInputStream(new byte[]{1}));
        assertEquals(2, result.getData().size());
        assertEquals("A A", result.getData().get(0).getTeacherName());
        assertEquals("C C", result.getData().get(1).getTeacherName());
    }

    @Test
    void excelToJsonThrowsWhenErrorsExist() throws Exception {
        ScheduleExcelReader reader = mock(ScheduleExcelReader.class);
        ScheduleTransform transform = mock(ScheduleTransform.class);
        ScheduleExcelWriter writer = mock(ScheduleExcelWriter.class);
        var s = new ScheduleEtlService(reader, transform, writer);
        var rows = List.of(dto("A A"));
        when(reader.read(any())).thenReturn(rows);
        when(transform.validate(rows)).thenReturn(List.of(new ValidationError(2, "type", "bad", ValidationError.Severity.ERROR)));
        assertThrows(IOException.class, () -> s.excelToJson(new ByteArrayInputStream(new byte[]{1})));
    }

    @Test
    void excelToJsonThrowsWhenWarningsExistToo() throws Exception {
        ScheduleExcelReader reader = mock(ScheduleExcelReader.class);
        ScheduleTransform transform = mock(ScheduleTransform.class);
        ScheduleExcelWriter writer = mock(ScheduleExcelWriter.class);
        var s = new ScheduleEtlService(reader, transform, writer);
        var rows = List.of(dto("A A"));
        when(reader.read(any())).thenReturn(rows);
        when(transform.validate(rows)).thenReturn(List.of(new ValidationError(2, "name", "warn", ValidationError.Severity.WARNING)));
        assertThrows(IOException.class, () -> s.excelToJson(new ByteArrayInputStream(new byte[]{1})));
    }

    @Test
    void writeToExcelDelegatesToWriter() throws Exception {
        ScheduleExcelReader reader = mock(ScheduleExcelReader.class);
        ScheduleTransform transform = mock(ScheduleTransform.class);
        ScheduleExcelWriter writer = mock(ScheduleExcelWriter.class);
        var s = new ScheduleEtlService(reader, transform, writer);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        s.writeToExcel(List.of(dto("A A")), out);
        verify(writer, times(1)).write(any(), eq(out));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "А Б", "Teacher One", "Петров П.П.", "John Smith", "Тест Тестов",
            "Имя Фамилия", "A B", "Name Last", "User User", "One Two",
            "X Y", "Alpha Beta"
    })
    void toJsonFromJsonRoundtripForDifferentNames(String teacher) throws Exception {
        ScheduleExcelReader reader = Mockito.mock(ScheduleExcelReader.class);
        ScheduleTransform transform = Mockito.mock(ScheduleTransform.class);
        ScheduleExcelWriter writer = Mockito.mock(ScheduleExcelWriter.class);
        var s = new ScheduleEtlService(reader, transform, writer);
        String json = s.toJson(List.of(dto(teacher)));
        var restored = s.fromJson(json);
        assertEquals(teacher, restored.get(0).getTeacherName());
    }
}
