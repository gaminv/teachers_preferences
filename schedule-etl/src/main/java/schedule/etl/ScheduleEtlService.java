package schedule.etl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import schedule.etl.api.ScheduleExcelReader;
import schedule.etl.api.ScheduleExcelWriter;
import schedule.etl.model.EtlResult;
import schedule.etl.model.SchedulePreferenceDto;
import schedule.etl.model.ValidationError;
import schedule.etl.transform.ScheduleTransform;

import schedule.etl.util.ValidationErrorReport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Оркестрация ETL: Excel ⇄ JSON для шаблона пожеланий расписания.
 * Импорт: Extract → Transform (нормализация + валидация) → JSON.
 * Экспорт: JSON → список DTO → Excel (XLSX).
 */
public final class ScheduleEtlService {

    private final ScheduleExcelReader reader;
    private final ScheduleTransform transform;
    private final ScheduleExcelWriter writer;
    private final ObjectMapper objectMapper;

    public ScheduleEtlService(ScheduleExcelReader reader,
                              ScheduleTransform transform,
                              ScheduleExcelWriter writer) {
        this.reader = reader;
        this.transform = transform;
        this.writer = writer;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Excel → извлечение, трансформация и валидация. Возвращает только валидные строки и отчёт об ошибках.
     * Строки с ошибками не ломают программу — они исключаются из данных, но попадают в отчёт.
     */
    public EtlResult excelToResult(InputStream excelInput) throws IOException {
        List<SchedulePreferenceDto> rows = reader.read(excelInput);
        List<ValidationError> errors = transform.validate(rows);
        // Исключаем из данных только строки с ошибками (ERROR); предупреждения (WARNING) не исключают строку
        Set<Integer> errorRowNumbers = errors.stream()
                .filter(e -> e.getSeverity() == ValidationError.Severity.ERROR)
                .map(ValidationError::getRowNumber)
                .collect(Collectors.toSet());
        List<SchedulePreferenceDto> validRows = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            int rowNum = i + 2; // в Excel строка 1 — заголовок
            if (!errorRowNumbers.contains(rowNum)) validRows.add(rows.get(i));
        }
        return new EtlResult(validRows, errors, rows);
    }

    /**
     * Импорт Excel → JSON. Возвращает JSON-строку только при успешной валидации;
     * иначе выбрасывает IOException с перечнем ошибок.
     */
    public String excelToJson(InputStream excelInput) throws IOException {
        EtlResult result = excelToResult(excelInput);
        if (!result.isSuccess()) {
            String msg = String.join("; ", result.getErrors());
            throw new IOException("Валидация не пройдена: " + msg);
        }
        return toJson(result.getData());
    }

    /**
     * Сериализация списка пожеланий в JSON (вход для алгоритмического ядра).
     */
    public String toJson(List<SchedulePreferenceDto> data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }

    /**
     * Десериализация JSON в список DTO (экспорт из ядра или из файла).
     */
    public List<SchedulePreferenceDto> fromJson(String json) throws JsonProcessingException {
        return objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, SchedulePreferenceDto.class));
    }

    /**
     * Экспорт списка пожеланий в Excel (XLSX) в заданный поток.
     */
    public void writeToExcel(List<SchedulePreferenceDto> data, OutputStream output) throws IOException {
        writer.write(data, output);
    }

    /**
     * Экспорт JSON → байты Excel (XLSX). Удобно для выгрузки по шаблону сайта.
     */
    public byte[] jsonToExcelBytes(String json) throws IOException {
        List<SchedulePreferenceDto> data = fromJson(json);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writer.write(data, out);
        return out.toByteArray();
    }

    /**
     * Записывает ошибки валидации в Excel-отчёт (сортировка: сначала ошибки, потом предупреждения; заливка красный/жёлтый; содержимое строк).
     *
     * @param path   путь к файлу отчёта (например, preferences-errors.xlsx)
     * @param result результат ETL (если успех — файл не создаётся)
     * @return путь к записанному файлу или null, если ошибок не было
     */
    public Path writeValidationErrorsToFile(Path path, EtlResult result) throws IOException {
        if (result == null || result.isSuccess()) {
            return null;
        }
        return ValidationErrorReport.writeToExcel(path, result.getValidationErrors(), result.getAllRows());
    }
}
