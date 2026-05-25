# LaborForce HRMS — Attendance & Overtime Engine

## Base HRMS Forked
amigoscode/spring-boot-fullstack-professional — chosen for its clean Spring Boot 3 + JPA + PostgreSQL structure with minimal boilerplate, making it easy to extend without fighting the existing code.

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- Redis (local: `docker run -d -p 6379:6379 redis` or any free cloud Redis)
- Supabase account (free tier at supabase.com)

### Supabase Setup
1. Create a new project at supabase.com
2. Go to Settings → Database → Connection Pooling
3. Copy the Connection Pooler URL (port 6543, PgBouncer) — NOT the direct connection (port 5432)
4. Format: `jdbc:postgresql://db.XXXX.supabase.co:6543/postgres?pgbouncer=true`

### Environment Variables
Create a `.env.local` file (already gitignored):

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://db.XXXX.supabase.co:6543/postgres?pgbouncer=true
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_password
REDIS_HOST=localhost
REDIS_PORT=6379
CORS_ALLOWED_ORIGINS=http://localhost:3000
```

### Run Locally (Windows PowerShell)
```powershell
./run-local.ps1
```

### Run Manually
```bash
mvn spring-boot:run
```

### Run Staging Profile
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=staging
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/v1/hrms/workers | Create worker |
| GET | /api/v1/hrms/workers | List active workers |
| POST | /api/v1/hrms/sites | Create site |
| GET | /api/v1/hrms/sites | List active sites |
| POST | /api/v1/hrms/attendance/clock-in | Clock in worker |
| POST | /api/v1/hrms/attendance/clock-out | Clock out, auto-calculates overtime |
| GET | /api/v1/hrms/attendance/active-workers | All clocked-in workers (from Redis) |
| GET | /api/v1/hrms/attendance/log | Paginated attendance history |
| GET | /api/v1/hrms/overtime/summary/{workerId}?month=YYYY-MM | Monthly overtime summary |
| POST | /api/v1/hrms/overtime/settle/{workerId}?month=YYYY-MM | Settle past month's overtime |

See `postman-collection.json` for importable examples with setup and edge-case requests.

## AI Tools Used
- GitHub Copilot: entity scaffolding, repository query design, service layer business logic, ticket fixes, README and Postman generation
- VS Code Copilot Chat: debugging, runtime verification, and configuration cleanup

## Design Decisions

### Schema
- BigDecimal for all wage/hour fields — never double/float to avoid payroll rounding errors
- DB-level unique constraint on (worker_id, clock_in) prevents duplicate clock-ins even under concurrent requests
- OvertimeEntry stores overtimeRate at time of creation for audit trail — wage rates can change, but historical payouts must not
- Index on (worker_id, date) on both AttendanceLog and OvertimeEntry for fast monthly queries

### Caching
- Redis stores only active workers (hot path, sub-millisecond reads for site supervisors)
- Redis is NOT the source of truth — DB is always the fallback
- 16-hour TTL as safety net for missed clock-outs
- CacheErrorHandler and guarded Redis calls let the app degrade to DB-only behavior if Redis is unavailable

### Transactions & Side Effects
- Settlement is fully atomic: all OvertimeEntries for a worker+month commit together or none do
- SMS fires via `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` — if DB rolls back, no SMS is sent; if SMS fails, settlement data is still correct
- External API calls happen before the transaction opens so DB connections are never held during network I/O

### Connection Pooling
- HikariCP `max-lifetime=270000` ms, below Supabase's 300s idle kill threshold
- `keepalive-time=60000` ms prevents idle connection drops
- Staging uses PgBouncer (port 6543) for connection multiplexing

## Ticket Fixes
| Ticket | Problem | Fix |
|--------|---------|-----|
| LF-201 | CORS blocking frontend | `SecurityConfig` uses externalized `demo.app.cors.allowed-origins`, and `.cors()` is enabled in the security filter chain |
| LF-202 | Redis down = app down | `CacheErrorHandler` plus guarded Redis calls and a 2s connect timeout keep the app running |
| LF-203 | Full table dump, N+1 | `JOIN FETCH` JPQL, pageable repository query, and `PagedResponse` wrapper |
| LF-204 | Partial settlement + early SMS | Single `@Transactional` settlement and `@TransactionalEventListener(AFTER_COMMIT)` SMS notification |
| LF-205 | Connection exhaustion on staging | HikariCP tuning plus non-transactional external API calls and timeout-based HTTP client config |

## What I'd Do Differently With More Time
- Row-level DB locking on clock-in for true concurrent safety (`SELECT FOR UPDATE`)
- WebSocket push for `/active-workers` instead of polling
- Scheduled batch job for month-end overtime calculation instead of real-time
- JWT auth with SUPERVISOR vs PAYROLL_OPERATOR roles
- Integration tests for overtime edge cases (60hr cap, 16hr flag, tiered rate)

## Submission Notes
- Local-only launcher: `run-local.ps1`
- Postman collection: `postman-collection.json`
- The app is configured for PostgreSQL by default and can degrade gracefully if Redis is unavailable
