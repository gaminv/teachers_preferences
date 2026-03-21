package schedule.etl.cli;

import schedule.etl.ScheduleEtlFactory;
import schedule.etl.ScheduleEtlService;
import schedule.etl.model.EtlResult;
import schedule.etl.model.SchedulePreferenceDto;

import schedule.etl.util.ValidationErrorReport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * CLI for ETL: import (Excel → JSON), export (JSON → Excel).
 * Все сгенерированные файлы по умолчанию сохраняются в папку output/.
 */
public final class ScheduleEtlCli {

    private static final String OUTPUT_DIR = "output";

    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            System.exit(1);
        }
        String command = args[0].toLowerCase();
        ScheduleEtlService service = ScheduleEtlFactory.createService();

        try {
            switch (command) {
                case "import" -> runImport(service, args);
                case "export" -> runExport(service, args);
                case "generate-admin-template" -> runGenerateAdminTemplate(service, args);
                default -> {
                    System.err.println("Неизвестная команда: " + command);
                    printUsage();
                    System.exit(1);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка ввода-вывода: " + e.getMessage());
            System.exit(2);
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(3);
        }
    }

    private static Path outputDir() {
        return Path.of(OUTPUT_DIR);
    }

    private static void ensureOutputDir() throws IOException {
        Files.createDirectories(outputDir());
    }

    private static void runImport(ScheduleEtlService service, String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Укажите файл Excel: import <файл.xlsx> [выход.json]");
            System.exit(1);
        }
        Path input = Path.of(args[1]);
        Path output = args.length >= 3 ? Path.of(args[2]) : outputDir().resolve(
                input.getFileName().toString().replaceAll("\\.xlsx?$", "") + ".json");

        if (!Files.isRegularFile(input)) {
            throw new IOException("Файл не найден: " + input);
        }

        ensureOutputDir();
        try (var in = Files.newInputStream(input)) {
            EtlResult result = service.excelToResult(in);
            String json = service.toJson(result.getData());
            Files.writeString(output, json);
            int validCount = result.getData().size();
            var issues = result.getValidationErrors();
            int issueCount = issues.size();
            if (issueCount > 0) {
                String baseName = input.getFileName().toString().replaceAll("\\.xlsx?$", "");
                Path errorsFile = outputDir().resolve(baseName + "-errors.xlsx");
                Path writtenReport = tryWriteReport(errorsFile, issues, result.getAllRows());
                result.getErrors().forEach(e -> System.err.println("[!] " + e));
                if (writtenReport != null) {
                    System.err.println("Подробный отчёт (Excel): " + writtenReport.toAbsolutePath());
                } else {
                    System.err.println("Не удалось записать Excel-отчёт (файл может быть открыт в Excel). Ошибки показаны в консоли.");
                }
                long errorRows = issues.stream()
                        .filter(e -> e.getSeverity() == schedule.etl.model.ValidationError.Severity.ERROR)
                        .map(schedule.etl.model.ValidationError::getRowNumber)
                        .distinct()
                        .count();
                long warningRows = issues.stream()
                        .filter(e -> e.getSeverity() == schedule.etl.model.ValidationError.Severity.WARNING)
                        .map(schedule.etl.model.ValidationError::getRowNumber)
                        .distinct()
                        .count();
                long issueRows = issues.stream()
                        .map(schedule.etl.model.ValidationError::getRowNumber)
                        .distinct()
                        .count();
                System.out.println("Импорт завершён. Загружено " + validCount + " записей.");
                System.out.println("  Замечания: " + issueRows + " строк (сообщений: " + issueCount + ")");
                System.out.println("  Ошибки: " + errorRows + " строк; Предупреждения: " + warningRows + " строк — см. файл отчёта.");
            } else {
                System.out.println("Импорт: " + input + " → " + output + " (" + validCount + " записей)");
            }
        }
    }

    private static Path tryWriteReport(Path preferredPath, List<schedule.etl.model.ValidationError> issues, List<SchedulePreferenceDto> allRows) {
        try {
            ValidationErrorReport.writeToExcel(preferredPath, issues, allRows);
            return preferredPath;
        } catch (IOException first) {
            // Частый кейс на Windows: файл отчёта открыт в Excel → нельзя перезаписать.
            // Пишем рядом новый файл с суффиксом времени.
            try {
                String name = preferredPath.getFileName().toString().replaceAll("\\.xlsx$", "");
                Path fallback = preferredPath.getParent().resolve(name + "-" + System.currentTimeMillis() + ".xlsx");
                ValidationErrorReport.writeToExcel(fallback, issues, allRows);
                return fallback;
            } catch (IOException second) {
                return null;
            }
        }
    }

    private static void runExport(ScheduleEtlService service, String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Укажите файл JSON: export <файл.json> [выход.xlsx]");
            System.exit(1);
        }
        Path input = Path.of(args[1]);
        Path output = args.length >= 3 ? Path.of(args[2]) : outputDir().resolve(
                input.getFileName().toString().replaceAll("\\.json$", "") + ".xlsx");

        if (!Files.isRegularFile(input)) {
            throw new IOException("Файл не найден: " + input);
        }

        ensureOutputDir();
        String json = Files.readString(input);
        List<SchedulePreferenceDto> data = service.fromJson(json);
        try (var out = Files.newOutputStream(output)) {
            service.writeToExcel(data, out);
        }
        System.out.println("Экспорт: " + input + " → " + output + " (" + data.size() + " записей)");
    }

    /**
     * Генерирует Excel по шаблону сайта для админа: валидные строки + строки с ошибками.
     * Удобно для ручной проверки импорта и отчёта (разные случаи в одном файле).
     */
    private static void runGenerateAdminTemplate(ScheduleEtlService service, String[] args) throws IOException {
        int total = 100;
        int invalidCount = 6;
        if (args.length >= 2) total = Integer.parseInt(args[1]);
        if (args.length >= 3) invalidCount = Integer.parseInt(args[2]);
        if (total <= 0 || total > 2000 || invalidCount < 0 || invalidCount >= total) {
            throw new IllegalArgumentException("Укажите: total от 1 до 2000, invalidCount от 0 до total-1");
        }
        ensureOutputDir();
        Path xlsxPath = outputDir().resolve("admin-test-template.xlsx");
        List<SchedulePreferenceDto> data = invalidCount == 0
                ? schedule.etl.util.SampleDataGenerator.generate(total)
                : schedule.etl.util.SampleDataGenerator.generateWithErrors(total, invalidCount);
        try (var out = Files.newOutputStream(xlsxPath)) {
            service.writeToExcel(data, out);
        }
        System.out.println("Создан файл по шаблону сайта: " + xlsxPath.toAbsolutePath());
        System.out.println("  Записей: " + total + (invalidCount > 0 ? " (из них " + invalidCount + " с ошибками для проверки валидации)" : ""));
        System.out.println("  Лист: Пожелания. Можно открыть, отредактировать и запустить: import output/admin-test-template.xlsx");
    }

    private static void printUsage() {
        System.err.println("""
                Schedule ETL — Excel ↔ JSON (шаблон пожеланий расписания)
                Файлы по умолчанию сохраняются в папку output/

                import <файл.xlsx> [выход.json]              Excel → JSON (при ошибках — *-errors.xlsx)
                export <файл.json> [выход.xlsx]              JSON → Excel
                generate-admin-template [total] [invalid]     Один Excel по шаблону (по умолч. 100 записей, 6 с ошибками)

                Примеры (из папки schedule-etl, JAR в target/):
                  java -jar target/schedule-etl.jar generate-admin-template
                  java -jar target/schedule-etl.jar import output/admin-test-template.xlsx
                  java -jar target/schedule-etl.jar export output/admin-test-template.json output/exported.xlsx
                """);
    }
}
