# System / End-to-End Test Plan (Teachers Preferences)

## Scope
- End-user business flows for teacher and administrator roles.
- Real browser interaction with the React frontend.
- Full request path through frontend, backend, authentication, and persistence.

## Environment
- CI job: `frontend-system` in `.github/workflows/project-tests.yml`.
- Runtime: Docker Compose (`db`, `backend`, `frontend`).
- Browser automation: Playwright + Chromium.

## Implemented scenarios
1. Teacher registration through the real UI.
2. Teacher login and redirect to the dashboard.
3. Opening semester preferences form from the dashboard.
4. Saving semester preferences through the live UI.
5. Viewing the saved semester preference from administrator UI.
6. Exporting preferences to Excel from administrator UI.
7. Saving session preferences through the live UI.
8. Persisting session preferences after page reload.
9. Invalid login attempt in the live UI.
10. Backend access denial without token.
11. Backend denial of teacher access to admin API.
12. Replacing existing preference set on repeated submit.
13. Semester and session data isolation.
14. Volume smoke scenario for larger preference batches.

## Key requirement status
- Real E2E: **yes**, frontend system tests no longer use mocked API.
- CI execution on pull request: **yes**, via Docker Compose + Playwright.
- Negative scenarios: **yes**, invalid login and unauthorized access are included.

## CI execution
- Backend system suite: `mvn -B -ntp test "-Dtest=*SystemTest,*SystemE2ETest"`
- Frontend real browser E2E: `npm run test:system`
