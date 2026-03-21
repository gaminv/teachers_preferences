# Testing Report (Frontend + Backend)

## Tools
- **Backend:** Maven, JUnit 5, Mockito, Spring Boot Test, JaCoCo.
- **Frontend:** Vitest, React Testing Library, jsdom.
- **CI:** GitHub Actions.

## Unit testing
- Coverage target: 80%+ (backend JaCoCo report generated on `verify`).
- Implemented 100+ unit tests across backend and frontend modules.
- Techniques:
  - equivalence classes,
  - boundary value analysis,
  - negative testing,
  - isolated mocking for dependencies.

## Coverage snapshot (without schedule-etl)
- Backend (JaCoCo, `mvn clean verify`):
  - Instruction: `91.75%`
  - Line: `90.50%`
  - Branch: `64.37%`
- Frontend (Vitest, `npm run test:coverage`):
  - Statements/Lines: `92.57%`
  - Branches: `79.02%`
  - Functions: `57.69%`

> Note: coverage above is calculated only for `backend` + `frontend`; `schedule-etl` is excluded.

## Integration testing
- 10+ integration scenarios implemented and documented.
- Includes positive and negative flows.
- CI job runs integration suites separately for backend and frontend.

## System / E2E testing
- 10+ system-level scenarios implemented.
- Includes business flow validation and volume smoke checks.
- CI job runs system suites separately for backend and frontend.

## Test suite extension procedure
1. Add unit tests near changed module (`backend/src/test/...` or `frontend/src/tests/unit`).
2. Add/update integration scenario to cover module interaction.
3. Add/update at least one system scenario for end-user flow.
4. Include negative case for each new rule/endpoint/component flow.
5. Validate in CI and keep docs updated (`docs/TEST_PLAN_*.md`).
