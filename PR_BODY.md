Feature: HRMS assignment implementation and fixes

Summary:
- Implemented attendance, overtime, worker, and site APIs with alias routes.
- Redis-backed active-worker cache with graceful degradation when Redis is unavailable.
- Overtime tiered rates (1.5x for first 2h, 2.0x thereafter), monthly cap, and transactional settlement.
- Duplicate clock-in returns 409; settling current month returns 400.
- Added `scripts/smoke-test.ps1` and updated README and Postman collection.

Smoke test output (abridged):
```
Creating site...
Site id: 5
Creating worker...
Worker id: 4
Clocking in...
Clock-in response: { ... }
Attempt duplicate clock-in (expect 409)...
Duplicate clock-in produced expected error: Conflict
Listing active workers...
Clocking out...
Clock-out response: { ... }
Fetching attendance log...
{ "totalElements": 1, "content": [ ... ] }
Attempt settling current month (expect 400)...
Settle produced expected error: The remote server returned an error: (400) Bad Request.
Smoke test complete.
```

Notes:
- Actuator shows Redis as DOWN when Redis isn't running; app degrades and core flows still operate.
- Tests passed locally: run `./mvnw.cmd -q test`.

Please review; merge into `main` when ready.
