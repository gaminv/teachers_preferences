## Schedule ETL (`schedule-etl`)

Модуль-конвертер **Excel ↔ JSON** для шаблона пожеланий расписания (лист **«Пожелания»**).

### Требования

- **Java 17** (JDK)
- **Maven 3.8+**

Если Maven/Java уже стоят — ничего отдельно “ставить зависимости” не нужно: Maven сам скачает их по `pom.xml`.

### Быстрый старт (с нуля)

Запускать команды из папки `schedule-etl`.

```bash
cd schedule-etl
mvn clean package
```

Дальше запускаем CLI (JAR лежит в `target/`):

```bash
java -jar target/schedule-etl.jar
```

### Команды CLI (их всего 3)

1) **Сгенерировать один Excel по шаблону**

```bash
java -jar target/schedule-etl.jar generate-admin-template
```

Создаст `output/admin-test-template.xlsx` (по умолчанию **100 строк**, из них **6 с ошибками** для проверки отчёта).

2) **Импортировать Excel → JSON (+ отчёт)**

```bash
java -jar target/schedule-etl.jar import output/admin-test-template.xlsx
```

Создаст:
- `output/admin-test-template.json` — **только валидные строки** (в шаблоне по умолчанию будет **94 записи**)
- `output/admin-test-template-errors.xlsx` — **отчёт** (если есть ошибки/предупреждения). Если файл отчёта открыт в Excel и его нельзя перезаписать, будет создан новый `*-errors-<timestamp>.xlsx`.

3) **Экспортировать JSON → Excel**

```bash
java -jar target/schedule-etl.jar export output/admin-test-template.json output/exported.xlsx
```

### Основные моменты модуля (самое важное)

- **Назначение**: конвертация **Excel ↔ JSON** для шаблона пожеланий расписания (лист **«Пожелания»**).
- **Шаблон для теста**: `generate-admin-template` создаёт один файл `output/admin-test-template.xlsx` (**100 строк**, из них **6 проблемных** для демонстрации отчёта).
- **Импорт не “падает” из‑за строк**: `import` всегда выдаёт JSON с валидными строками; строки с **ERROR** в JSON не попадают, строки с **WARNING** — попадают.
- **Отчёт об ошибках**: создаётся `*-errors.xlsx` (сначала ошибки, потом предупреждения; строки подсвечены красным/жёлтым; показано содержимое проблемной строки).
- **Валидация (3 уровня)**:
  - **структурная (Extract)**:
    - лист **«Пожелания»** (или первый лист) и первый заголовок содержит **«Преподаватель»**,
  - **Bean Validation (DTO: `SchedulePreferenceDto`)**:
    - `teacherName` (**ФИО**) — обязательно (не пустое),
    - `type` (**Тип**) — только `semester` или `session` (в Excel: **«Семестр»/«Сессия»**),
    - диапазоны приоритетов **0–5** для полей (**ошибка**, строка не попадает в JSON):
      - `daysPriority`, `timesPriority`, `loadTypePriority`, `buildingRoomPriority`, `boardTypePriority`, `computersPriority`, `formatPriority`, `commentsPriority`,
    - строковые поля дополнительно ограничены по длине (**предупреждение**, строка остаётся в JSON): слишком длинные значения попадают в отчёт, но не блокируют импорт,
  - **бизнес‑правила (Transform)**:
    - ФИО “одним словом” (без пробела) — **WARNING** (строка остаётся в данных, но попадает в отчёт).

### Тестирование (unit / integration / system)

Запуск всех тестов и покрытия:

```bash
mvn clean verify
```

Запуск только модульных тестов:

```bash
mvn test "-Dtest=*Unit*,*ScheduleEtlServiceTest"
```

Запуск только интеграционных сценариев:

```bash
mvn test "-Dtest=*IntegrationTest"
```

Запуск только системных/E2E сценариев:

```bash
mvn test "-Dtest=*SystemE2ETest"
```

Тест-планы и отчёт:
- `docs/TEST_PLAN_INTEGRATION.md`
- `docs/TEST_PLAN_SYSTEM_E2E.md`
- `docs/TESTING_REPORT.md`

