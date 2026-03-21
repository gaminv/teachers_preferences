# System / End-to-End Test Plan

## Scope
- End-to-end validation of the ETL product against business behavior:
  - Excel template processing
  - Validation report generation
  - JSON compatibility for downstream scheduling core
  - File-based workflow stability

## Test scenarios (minimum 10)
1. Generate template-like data, import and export pipeline runs to completion.
2. Validation report file is generated for warning cases.
3. Invalid rows are excluded from final dataset.
4. Fully valid dataset completes without validation issues.
5. JSON output contains required business fields and is readable back.
6. Full filesystem roundtrip through temporary JSON/XLSX files.
7. Long comment values do not break pipeline.
8. Warning-only rows remain serializable to JSON.
9. Multiple conversion iterations are stable by record count.
10. Performance smoke: 300 rows processed under an acceptable threshold.

## Design techniques used
- Main user-flow scenarios.
- Negative system behavior (invalid rows in input).
- Volume/performance smoke scenario.

## CI execution
- Trigger: manual workflow dispatch and on-demand branch run.
- Command: `mvn -B -ntp test "-Dtest=*SystemE2ETest"`.
