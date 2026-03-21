package schedule.etl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Результат ETL: данные и/или детализированный отчёт об ошибках валидации.
 * Поддерживает многоуровневую валидацию (ошибки с привязкой к строке и полю).
 */
public final class EtlResult {

    private final List<SchedulePreferenceDto> data;
    private final List<ValidationError> validationErrors;
    private final List<SchedulePreferenceDto> allRows;

    public EtlResult(List<SchedulePreferenceDto> data, List<ValidationError> validationErrors) {
        this(data, validationErrors, null);
    }

    public EtlResult(List<SchedulePreferenceDto> data, List<ValidationError> validationErrors, List<SchedulePreferenceDto> allRows) {
        this.data = data != null ? new ArrayList<>(data) : new ArrayList<>();
        this.validationErrors = validationErrors != null ? new ArrayList<>(validationErrors) : new ArrayList<>();
        this.allRows = allRows != null ? new ArrayList<>(allRows) : null;
    }

    public static EtlResult success(List<SchedulePreferenceDto> data) {
        return new EtlResult(data, Collections.emptyList());
    }

    public static EtlResult failure(List<ValidationError> errors) {
        return new EtlResult(Collections.emptyList(), errors);
    }

    /** Совместимость: создать результат с ошибками, заданными строками сообщений. */
    public static EtlResult failureFromMessages(List<String> errorMessages) {
        List<ValidationError> list = errorMessages != null
                ? errorMessages.stream().map(msg -> new ValidationError(0, "", msg)).collect(Collectors.toList())
                : Collections.emptyList();
        return new EtlResult(Collections.emptyList(), list);
    }

    public List<SchedulePreferenceDto> getData() {
        return data;
    }

    /** Все строки из Excel (для отчёта с содержимым проблемных строк). Может быть null. */
    public List<SchedulePreferenceDto> getAllRows() {
        return allRows == null ? null : Collections.unmodifiableList(allRows);
    }

    /** Детализированные ошибки валидации (строка, поле, сообщение). */
    public List<ValidationError> getValidationErrors() {
        return Collections.unmodifiableList(validationErrors);
    }

    /** Сообщения об ошибках в виде строк (для вывода в stderr, совместимость). */
    public List<String> getErrors() {
        return validationErrors.stream()
                .map(ValidationError::toString)
                .collect(Collectors.toList());
    }

    public boolean isSuccess() {
        return validationErrors.isEmpty();
    }
}
