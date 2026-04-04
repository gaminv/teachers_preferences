# Integration Test Plan (Teachers Preferences)

## Scope
- **Backend integration:** auth flow, teacher protected endpoints, role-based admin restrictions, replacement logic by `type`.
- **Frontend integration:** route composition and registration flow with controlled mock isolation.

## Implemented scenarios
1. Duplicate registration rejected (`400`).
2. Invalid login credentials rejected (`401`).
3. Teacher endpoint without token forbidden.
4. Teacher saves semester preferences.
5. Save + get returns the same count.
6. Save replaces old records for the same `type`.
7. Admin endpoint denied for teacher token.
8. Semester and session data isolation.
9. Frontend route `/teacher/:type` renders the form module.
10. Frontend unknown route renders a fallback page.
11. Frontend registration success flow with mocked API response.
12. Frontend registration negative flow with mocked API error.

## Negative scenarios
- Invalid credentials.
- Missing or invalid auth token.
- Forbidden role access to admin API.
- Registration request rejected by backend/API layer.

## Mocking and isolation
- Backend integration tests use real Spring Boot HTTP layer with in-memory DB.
- Frontend integration tests use mocks only for route-level isolation and registration API behavior.
- System/E2E layer is separated and does not use API mocks.

## CI execution
- Backend integration: `mvn -B -ntp test "-Dtest=*IntegrationTest"`
- Frontend integration: `npm run test:integration`
