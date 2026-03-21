package schedule.etl.unit;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import schedule.etl.model.SchedulePreferenceDto;
import schedule.etl.model.ValidationError;
import schedule.etl.transform.ScheduleTransform;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class ScheduleTransformUnitTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final ScheduleTransform transform = new ScheduleTransform(validator);

    private SchedulePreferenceDto validDto() {
        SchedulePreferenceDto dto = new SchedulePreferenceDto();
        dto.setTeacherName("Иванов И. И.");
        dto.setTeacherLogin("ivanov");
        dto.setType("semester");
        dto.setSubject("Математика");
        return dto;
    }

    @ParameterizedTest
    @CsvSource({
            "0,true", "1,true", "2,true", "3,true", "4,true", "5,true",
            "-1,false", "6,false", "99,false"
    })
    void daysPriorityBoundaries(int value, boolean valid) {
        SchedulePreferenceDto dto = validDto();
        dto.setDays(List.of("Пн"));
        dto.setDaysPriority(value);
        boolean hasError = transform.validate(List.of(dto)).stream()
                .anyMatch(e -> e.getField().contains("daysPriority") && e.getSeverity() == ValidationError.Severity.ERROR);
        assertEquals(!valid, hasError);
    }

    @ParameterizedTest
    @CsvSource({
            "0,true", "1,true", "2,true", "3,true", "4,true", "5,true",
            "-1,false", "6,false", "100,false"
    })
    void timesPriorityBoundaries(int value, boolean valid) {
        SchedulePreferenceDto dto = validDto();
        dto.setTimes("до 14:00");
        dto.setTimesPriority(value);
        boolean hasError = transform.validate(List.of(dto)).stream()
                .anyMatch(e -> e.getField().contains("timesPriority") && e.getSeverity() == ValidationError.Severity.ERROR);
        assertEquals(!valid, hasError);
    }

    @ParameterizedTest
    @CsvSource({
            "0,true", "1,true", "2,true", "3,true", "4,true", "5,true",
            "-1,false", "6,false", "100,false"
    })
    void loadPriorityBoundaries(int value, boolean valid) {
        SchedulePreferenceDto dto = validDto();
        dto.setLoadType("compact");
        dto.setLoadTypePriority(value);
        boolean hasError = transform.validate(List.of(dto)).stream()
                .anyMatch(e -> e.getField().contains("loadTypePriority") && e.getSeverity() == ValidationError.Severity.ERROR);
        assertEquals(!valid, hasError);
    }

    @ParameterizedTest
    @CsvSource({
            "0,true", "1,true", "2,true", "3,true", "4,true", "5,true",
            "-1,false", "6,false", "100,false"
    })
    void buildingPriorityBoundaries(int value, boolean valid) {
        SchedulePreferenceDto dto = validDto();
        dto.setBuildingRoom("Корпус 1");
        dto.setBuildingRoomPriority(value);
        boolean hasError = transform.validate(List.of(dto)).stream()
                .anyMatch(e -> e.getField().contains("buildingRoomPriority") && e.getSeverity() == ValidationError.Severity.ERROR);
        assertEquals(!valid, hasError);
    }

    @ParameterizedTest
    @CsvSource({
            "0,true", "1,true", "2,true", "3,true", "4,true", "5,true",
            "-1,false", "6,false", "100,false"
    })
    void boardPriorityBoundaries(int value, boolean valid) {
        SchedulePreferenceDto dto = validDto();
        dto.setBoardType("marker");
        dto.setBoardTypePriority(value);
        boolean hasError = transform.validate(List.of(dto)).stream()
                .anyMatch(e -> e.getField().contains("boardTypePriority") && e.getSeverity() == ValidationError.Severity.ERROR);
        assertEquals(!valid, hasError);
    }

    @ParameterizedTest
    @CsvSource({
            "0,true", "1,true", "2,true", "3,true", "4,true", "5,true",
            "-1,false", "6,false", "100,false"
    })
    void computersPriorityBoundaries(int value, boolean valid) {
        SchedulePreferenceDto dto = validDto();
        dto.setComputers(List.of("Windows"));
        dto.setComputersPriority(value);
        boolean hasError = transform.validate(List.of(dto)).stream()
                .anyMatch(e -> e.getField().contains("computersPriority") && e.getSeverity() == ValidationError.Severity.ERROR);
        assertEquals(!valid, hasError);
    }

    @ParameterizedTest
    @CsvSource({
            "0,true", "1,true", "2,true", "3,true", "4,true", "5,true",
            "-1,false", "6,false", "100,false"
    })
    void formatPriorityBoundaries(int value, boolean valid) {
        SchedulePreferenceDto dto = validDto();
        dto.setFormat("in-person");
        dto.setFormatPriority(value);
        boolean hasError = transform.validate(List.of(dto)).stream()
                .anyMatch(e -> e.getField().contains("formatPriority") && e.getSeverity() == ValidationError.Severity.ERROR);
        assertEquals(!valid, hasError);
    }

    @ParameterizedTest
    @CsvSource({
            "0,true", "1,true", "2,true", "3,true", "4,true", "5,true",
            "-1,false", "6,false", "100,false"
    })
    void commentsPriorityBoundaries(int value, boolean valid) {
        SchedulePreferenceDto dto = validDto();
        dto.setComments("ok");
        dto.setCommentsPriority(value);
        boolean hasError = transform.validate(List.of(dto)).stream()
                .anyMatch(e -> e.getField().contains("commentsPriority") && e.getSeverity() == ValidationError.Severity.ERROR);
        assertEquals(!valid, hasError);
    }

    @Test
    void invalidTypeProducesError() {
        SchedulePreferenceDto dto = validDto();
        dto.setType("year");
        var errors = transform.validate(List.of(dto));
        assertTrue(errors.stream().anyMatch(e -> e.getField().contains("type")));
    }

    @Test
    void oneWordTeacherNameProducesWarning() {
        SchedulePreferenceDto dto = validDto();
        dto.setTeacherName("Иванов");
        var errors = transform.validate(List.of(dto));
        assertTrue(errors.stream().anyMatch(e -> e.getSeverity() == ValidationError.Severity.WARNING));
    }

    @Test
    void trimsAndRemovesEmptyDays() {
        SchedulePreferenceDto dto = validDto();
        dto.setDays(List.of(" Пн ", " ", "Пн", "Вт "));
        transform.validate(List.of(dto));
        assertEquals(List.of("Пн", "Вт"), dto.getDays());
    }

    @Test
    void trimsComputersButKeepsDuplicates() {
        SchedulePreferenceDto dto = validDto();
        dto.setComputers(List.of(" Windows ", "Linux", "Windows"));
        transform.validate(List.of(dto));
        assertEquals(List.of("Windows", "Linux", "Windows"), dto.getComputers());
    }
}
