package schedule.etl.model;

/**
 * Одна ошибка валидации с привязкой к строке и полю (для детализированного отчёта).
 */
public final class ValidationError {

    private final int rowNumber;
    private final String field;
    private final String message;
    private final Severity severity;

    public enum Severity {
        ERROR,
        WARNING
    }

    public ValidationError(int rowNumber, String field, String message) {
        this(rowNumber, field, message, Severity.ERROR);
    }

    public ValidationError(int rowNumber, String field, String message, Severity severity) {
        this.rowNumber = rowNumber;
        this.field = field != null ? field : "";
        this.message = message != null ? message : "";
        this.severity = severity != null ? severity : Severity.ERROR;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }

    public Severity getSeverity() {
        return severity;
    }

    /** Строка для вывода в консоль (с указанием типа: ошибка или предупреждение). */
    @Override
    public String toString() {
        String prefix = severity == Severity.WARNING ? "[Предупреждение] " : "";
        return prefix + "Строка " + rowNumber + ": " + (field.isEmpty() ? "" : field + " — ") + message;
    }
}
