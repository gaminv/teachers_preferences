# Testing Report

## Tools and stack
- Build/Test runner: Maven Surefire.
- Unit testing: JUnit 5.
- Mocking: Mockito.
- Coverage: JaCoCo (`mvn verify` -> `target/site/jacoco`).
- CI: GitHub Actions workflow.

## Completed work
- Implemented full automated testing suite:
  - Unit tests: 100+ (target for 4 team members satisfied).
  - Integration scenarios: 10.
  - System/E2E scenarios: 10.
- Added tagging by level: `unit`, `integration`, `system`.
- Added CI jobs with separate execution per test level.
  - Unit: `mvn test "-Dtest=*Unit*,*ScheduleEtlServiceTest"` (103 tests).
  - Integration: `mvn test "-Dtest=*IntegrationTest"` (10 tests).
  - System/E2E: `mvn test "-Dtest=*SystemE2ETest"` (10 tests).

## Test design techniques used
- Equivalence partitioning:
  - valid/invalid `type`,
  - valid/invalid priorities.
- Boundary value analysis:
  - priority range checks `0..5`,
  - out-of-range checks (`-1`, `6`, `99`).
- Negative scenarios:
  - malformed rows, invalid domain values.

## Coverage and quality target
- JaCoCo integrated to measure line/branch coverage.
- Target: 80%+ code coverage for production classes.
- Command:
  - `mvn clean verify`
  - open `target/site/jacoco/index.html`.

## How to extend tests when new module/method is added
1. Add new unit tests in `src/test/java/schedule/etl/unit`.
2. Add/update integration scenario in `src/test/java/schedule/etl/integration`.
3. Add/update one E2E flow in `src/test/java/schedule/etl/system`.
4. Add boundary and negative cases for new rules.
5. Ensure tags are set (`@Tag("unit"|"integration"|"system")`).
6. Run `mvn clean verify` and confirm JaCoCo and CI green.
