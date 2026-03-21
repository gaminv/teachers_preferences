# System / End-to-End Test Plan (Whole Project)

## Scope
- Business-level checks for complete backend user flows (register/login/persist/get preferences).
- Frontend user-level forms and interactions (registration flow and error handling).
- Security and stability checks in realistic request chains.

## Implemented scenarios (10+)
1. Register + login full flow.
2. Save/read semester preferences flow.
3. Save/read session preferences flow.
4. Access denial without token.
5. Invalid login rejected.
6. Multi-record persistence.
7. Replace semester records by re-submit.
8. Cross-type isolation.
9. Admin endpoint denied for non-admin.
10. Volume smoke scenario (20 records).
11. Frontend registration success flow.
12. Frontend registration negative flow.

## Performance / volume applicability
- Implemented **volume smoke** scenario in backend system tests.
- Baseline timing assertion to detect severe regressions.

## CI execution
- Backend system: `mvn -B -ntp test "-Dtest=*SystemTest,*SystemE2ETest"`
- Frontend system: `npm run test:system`
