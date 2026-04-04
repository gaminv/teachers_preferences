# Testing Report (Teachers Preferences)

## Tools
- **Backend:** Maven, JUnit 5, Mockito, Spring Boot Test, TestRestTemplate, JaCoCo.
- **Frontend:** Vitest, React Testing Library, Playwright.
- **Runtime / CI:** Docker Compose, GitHub Actions, CodeRabbit.

## Unit testing
- Backend unit tests: `78`.
- Frontend unit tests: `60`.
- Total unit tests in main application: `138`.
- Automatic execution:
  - `mvn test` for backend,
  - `npm run test:unit` for frontend,
  - GitHub Actions workflow `.github/workflows/project-tests.yml`.
- Applied test-design techniques:
  - equivalence classes,
  - boundary value analysis,
  - negative testing,
  - DTO / mapping verification,
  - dependency isolation with mocks.

## Coverage snapshot
- Backend (`backend/target/site/jacoco/jacoco.csv`):
  - Instruction: `91.75%`
  - Line: `90.50%`
  - Branch: `64.37%`
- Frontend (`frontend/coverage/coverage-summary.json`):
  - Statements / Lines: `92.57%`
  - Branches: `79.02%`
  - Functions: `57.69%`

## Integration testing
- Backend integration scenarios: `10`.
- Frontend integration scenarios: `17`.
- Covered flows:
  - duplicate registration,
  - invalid login,
  - unauthorized access to protected endpoints,
  - preference save / read flows,
  - role checks,
  - route-level composition in frontend.
- Mocks are used only on the integration level where isolation is required.

## System / End-to-End testing
- Backend system HTTP scenarios: `10`.
- Frontend browser E2E scenarios: `4`.
- Frontend system tests now run through **Playwright** against a live stack and do **not** mock backend API calls.
- CI job `frontend-system`:
  - installs Playwright,
  - starts `db`, `backend`, and `frontend` through Docker Compose,
  - waits for application readiness,
  - runs `npm run test:system`,
  - uploads Playwright artifacts.

## CodeRabbit / PR pipeline
- Added `.coderabbit.yaml` to prevent long pending states on PRs.
- Key changes:
  - auto-review enabled for any base branch,
  - GitHub checks waiting inside CodeRabbit disabled,
  - generated artifacts excluded from CodeRabbit review scope,
  - review status remains visible in pull requests.

## Test suite extension procedure
1. Add unit tests in `backend/src/test/...` or `frontend/src/tests/unit`.
2. Add or update an integration scenario for module interaction.
3. Add or update at least one Playwright or backend system scenario for the end-user flow.
4. Include a negative case for every new business rule or protected endpoint.
5. Run the corresponding CI jobs and update `docs/TEST_PLAN_*.md` and `.docx` reports.
