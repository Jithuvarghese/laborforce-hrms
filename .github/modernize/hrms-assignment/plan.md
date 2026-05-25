# HRMS Assignment Implementation Plan

## Plan Metadata
- Workspace: `C:/Users/91918/Documents/Deepthought/spring-boot-fullstack-professional`
- Language: `java`
- Stack: `Spring Boot + Spring Data JPA + PostgreSQL + React frontend`
- Plan type: `direct multi-task plan (assessment skipped)`
- Branch strategy: `feature/hrms-assignment`

## Preconditions
1. Ensure local working tree is clean or stash unrelated changes.
2. Create and switch branch:
   - `git checkout -b feature/hrms-assignment`
3. Ensure Java and Maven wrapper are available.
4. Build baseline before changes:
   - `./mvnw.cmd -q -DskipTests compile`

## Execution Phases

### Phase 1 - Config and Dependency Foundation
Scope:
- Update `pom.xml` with Redis/cache/validation dependencies as needed.
- Keep Spring Boot at `>= 3.1.x` (already `3.4.6`; validate no downgrade).
- Migrate property configuration from `.properties` to `application.yml`.
- Add `application-staging.yml` with Hikari, Redis, CORS, and pagination settings.

Required commit:
- `chore: migrate to application.yml, add Redis/HikariCP/Cache config`

Validation gate:
- `./mvnw.cmd -q -DskipTests compile`

### Phase 2 - Workforce Domain Model
Scope:
- Add entities: `Worker`, `Site`, `AttendanceLog`, `OvertimeEntry`.
- Add enums: `Designation`, `SettlementStatus`.
- Apply required constraints, indices, timestamps, and JPA mappings.
- Add DB migration alignment if project uses schema-first updates.

Required commit:
- Use the commit message specified by user for Phase 2 domain modeling step.

Validation gate:
- `./mvnw.cmd -q -DskipTests compile`

### Phase 3 - Repository Layer
Scope:
- Add repositories:
  - `WorkerRepository`
  - `SiteRepository`
  - `AttendanceLogRepository`
  - `OvertimeEntryRepository`
- Implement JPQL with `JOIN FETCH` for N+1 prevention where required.
- Implement monthly overtime aggregation query (sum by worker/month).

Required commit:
- Use the commit message specified by user for Phase 3 repository step.

Validation gate:
- `./mvnw.cmd -q -DskipTests compile`

### Phase 4 - Redis Cache Layer
Scope:
- Add `RedisConfig` with graceful degradation behavior.
- Add cache error handling with `CacheErrorHandler` + `CachingConfigurer` + `@EnableCaching`.
- Add `ActiveWorkerCacheService` with safe defaults and Redis exception handling.

Required commit:
- Use the commit message specified by user for Phase 4 caching step.

Validation gate:
- `./mvnw.cmd -q -DskipTests compile`

### Phase 5 - DTOs and Exception Handling
Scope:
- Add request/response/paged DTOs.
- Add `HrmsException` hierarchy.
- Add `GlobalExceptionHandler` with structured API error responses.

Required commit:
- Use the commit message specified by user for Phase 5 DTO/exception step.

Validation gate:
- `./mvnw.cmd -q -DskipTests compile`

### Phase 6 - Services, Rules, and Events
Scope:
- Add `AttendanceService` and `OvertimeService`.
- Enforce business rules:
  - clock-in/clock-out flow integrity
  - overtime cap `60h`
  - tiered overtime rates
  - settlement atomicity and transactional safety
- Add `OvertimeSettledEvent`.
- Add `SmsNotificationListener` with `@TransactionalEventListener(phase = AFTER_COMMIT)` behavior.

Required commit:
- Use the commit message specified by user for Phase 6 service/event step.

Validation gate:
- `./mvnw.cmd -q -DskipTests compile`

### Phase 7 - API Controllers
Scope:
- Add controllers:
  - `AttendanceController`
  - `OvertimeController`
  - `WorkerController`
  - `SiteController`
- Implement required endpoints and pagination parameters.
- Ensure validation annotations and error mapping are wired.

Required commit:
- Use the commit message specified by user for Phase 7 controller step.

Validation gate:
- `./mvnw.cmd -q -DskipTests compile`

### Phase 8 - LF-201 to LF-205 Fixes
Scope:
- LF-201: Security CORS configuration hardening.
- LF-202: Redis resilience hardening.
- LF-203: Pagination + N+1 verification.
- LF-204: transactional settlement comments and guardrails.
- LF-205: HikariCP tuning + external API call refactor + `RestTemplate` timeouts.

Required commits:
- Use the commit messages specified by user for LF-201..LF-205 fixes.

Validation gate:
- `./mvnw.cmd -q -DskipTests compile`

### Phase 9 - Documentation and Delivery
Scope:
- Rewrite `README.md` for setup and HRMS module usage.
- Add `curl-examples.md` with endpoint examples.
- Run final compile check and resolve imports/issues.
- Final commit and push:
  - `git push origin feature/hrms-assignment`

Required commit:
- Use the final documentation commit message specified by user.

Validation gate:
- `./mvnw.cmd -q -DskipTests compile`

## Cross-Cutting Quality Gates
1. Compile check after every phase.
2. Resolve import/package issues immediately after each phase.
3. Keep commits phase-scoped and minimal.
4. Verify no N+1 regressions for repository/controller flows touched in Phases 3/7/8.

## Deliverables
1. Branch `feature/hrms-assignment` containing phased commits.
2. HRMS domain, repositories, caching, services, controllers, and docs.
3. Updated config in YAML profiles with staging-specific overrides.

## Known Blockers Before Execution
1. Exact commit message strings are only explicitly provided for Phase 1 in the request; Phases 2-9 say "as specified" but do not include literal messages in this prompt.
2. Detailed field-level constraints/indices/timestamp specifications for entities are referenced as "specified" but not included in this prompt.
3. Required endpoint contracts and exact pagination defaults are requested but not fully enumerated in this prompt.