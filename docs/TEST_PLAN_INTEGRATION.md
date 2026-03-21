# Integration Test Plan (Backend + Frontend)

## Scope
- **Backend integration:** auth, JWT-protected teacher endpoints, role-based admin restrictions, data replacement logic by `type`.
- **Frontend integration:** route-level composition and module interaction (routing + page rendering contracts).

## Implemented scenarios (10+)
1. Duplicate registration rejected (`400`).
2. Invalid login credentials rejected (`401`).
3. Teacher endpoint without token forbidden.
4. Teacher saves semester preferences.
5. Save + get returns same count.
6. Save replaces old records for same `type`.
7. Admin endpoint denied for teacher token.
8. Semester/session data isolation.
9. Frontend route `/teacher/:type` renders form module.
10. Frontend unknown route renders fallback page.

## Negative scenarios
- Invalid credentials.
- Missing/invalid auth token.
- Forbidden role access to admin API.

## Mocking and isolation
- Backend: repository/service layers mocked in unit-level integration boundaries.
- Frontend: component and API mocks for route-level integration tests.

## CI execution
- Backend integration: `mvn -B -ntp test "-Dtest=*IntegrationTest"`
- Frontend integration: `npm run test:integration`
