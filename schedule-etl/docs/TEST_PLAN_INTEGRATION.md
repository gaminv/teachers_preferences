# Integration Test Plan

## Scope
- ETL chain integration: `Reader -> Transform -> Service -> Writer`.
- Data and error-flow consistency between modules.
- Negative scenarios with invalid type, invalid priorities and malformed rows.

## Test scenarios (minimum 10)
1. Valid minimal data imports successfully.
2. Rows with `ERROR` are excluded from resulting dataset.
3. Rows with `WARNING` stay in resulting dataset.
4. `excelToJson` fails when validation has errors.
5. JSON export/import preserves row count.
6. Type mapping (`Семестр`/`Сессия`) maps to internal values.
7. Priority fields roundtrip from writer to reader.
8. Multi-value `computers` roundtrip works.
9. Empty dataset is processed as a valid edge case.
10. Large dataset (200 rows) is processed without data loss.

## Mocking and isolation
- Unit-level integration boundaries use mocked `ScheduleExcelReader`, `ScheduleTransform`, `ScheduleExcelWriter`.
- Full integration scenarios use real implementation classes.
- External services are absent; filesystem interactions use temporary files only.

## Design techniques used
- Equivalence classes (valid/invalid types and priorities).
- Boundary values (priority 0..5 and out-of-range values).
- Negative testing (invalid type, invalid priorities).

## CI execution
- Trigger: on push and pull request.
- Command: `mvn -B -ntp test "-Dtest=*IntegrationTest"`.
