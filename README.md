# englishmoon-api

Spring Boot 3.x + Kotlin service that powers the EnglishMoon platform. The codebase follows a clean architecture layout (domain → application → infrastructure) and embraces a contract-first workflow driven by `openapi.yaml`.

## Highlights
- Kotlin + Spring Web/Data/Security with JWT bearer access tokens and HttpOnly refresh cookies.
- PostgreSQL 16 schema versioned with Flyway.
- Contract-first generators: backend interfaces via OpenAPI Generator, frontend types via `openapi-typescript`.
- Testcontainers-backed integration tests for realistic DAO and controller coverage (opt-in when Docker is present).
- Structured packaging: `domain`, `app`, and `infra` layers keep concerns isolated and testable.
- Kotlin Flow endpoints (e.g. `/courses/stream`) for streaming-friendly slices.
- Lessons, enrollments, quizzes, and articles are backed by persistent repositories (no more static stubs).
- Built-in rate limiting on high-risk public endpoints (`/auth/forgot-password`, `/auth/login`).

## Project Layout
- `src/main/kotlin/com/englishmoon/domain` – pure domain entities and ports.
- `src/main/kotlin/com/englishmoon/app` – application use cases orchestrating ports.
- `src/main/kotlin/com/englishmoon/infra` – adapters (web, persistence, security) and configuration.
- `src/main/resources/application.yaml` – layered configuration defaults.
- `src/main/resources/db/migration` – Flyway migrations (`V__` for versioned DDL/DML, `R__` for repeatable scripts).
- `build/generated/openapi` – OpenAPI-generated interfaces (excluded from linting; regenerated as needed).

## Prerequisites
- JDK 21 (minimum 17) with Gradle toolchain support.
- Docker Desktop (or compatible) for running integration tests locally.
- PostgreSQL 16 if not using the provided docker-compose recipe.

## Quickstart
1. **Install dependencies**
   ```bash
   ./gradlew --version
   ```
2. **Bring up PostgreSQL** (optional helper):
   ```bash
   docker compose -f ../docker-compose.local.yml up -d postgres
   ```
3. **Copy & load environment variables**
   ```bash
   cp .env.example .env
   # edit .env with real secrets (or export manually)
   export $(grep -v '^#' .env | xargs)  # or use direnv / IDE EnvFile plugin
   ```
4. **Apply migrations & run the app**
   ```bash
   ./gradlew bootRun
   ```
   Service listens on `http://localhost:8080`. Health probe: `GET /health`.

5. **Generate OpenAPI server interfaces** whenever the contract changes:
   ```bash
   ./gradlew generateOpenApiKotlin
   ```

## Core Endpoints
- `GET /health` — liveness probe.
- `GET /courses` — list courses (supports `page` & `size`).
- `GET /courses/{id}` — fetch a course by UUID.
- `POST /courses` — create a course (JWT required).
- `PATCH /courses/{id}` — update mutable fields (JWT required).
- `GET /lessons` — list lessons, filterable by `courseId`.
- `POST /enrollments` — enroll a learner into a course (JWT required).
- `GET /quizzes` — list quizzes, filterable by `lessonId`, including ordered questions.
- `POST /users` — register a learner (public).
- `POST /auth/login` — exchange credentials for access + refresh tokens.
- `POST /auth/refresh` — rotate refresh cookie and obtain a new access token.
- `GET /articles` — list marketing articles persisted via Flyway seeds.
- `GET /articles/{slug}` — fetch a single marketing article.

## Useful Gradle Tasks
- `./gradlew ktlintCheck` – Kotlin style enforcement.
- `./gradlew test -PskipIntegrationTests=true` – fast unit suite (excludes Docker/Testcontainers).
- `./gradlew integrationTest` – runs tests tagged `integration`; automatically skips when Docker is unavailable thanks to `@Testcontainers(disabledWithoutDocker = true)`.
- `./gradlew bootRun` – start the API with dev config.
- `./gradlew clean build` – full build with tests + packaging.

You can combine lint + fast tests in CI-style fashion:
```bash
./gradlew ktlintCheck test -PskipIntegrationTests=true
```

## Configuration Cheat Sheet
Environment variables (or Spring properties) override defaults:
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- `SECURITY_JWT_SECRET` – signing key (required in non-dev environments).
- `SECURITY_JWT_ACCESS_TOKEN_TTL_MINUTES` – access token lifetime (default 15).
- `SECURITY_JWT_REFRESH_COOKIE_SECURE`, `SECURITY_JWT_REFRESH_COOKIE_DOMAIN`, `SECURITY_JWT_REFRESH_COOKIE_SAMESITE`
- `SECURITY_JWT_REFRESH_TOKEN_TTL_DAYS`, `SECURITY_JWT_MAX_REFRESH_TOKENS_PER_USER`, `SECURITY_JWT_REFRESH_COOKIE_NAME`
- `SPRING_PROFILES_ACTIVE=local` to load any profile-specific overrides you add.
- `SECURITY_PASSWORD_RESET_TOKEN_TTL` (ISO-8601 duration) and `SECURITY_PASSWORD_RESET_BASE_URL` to shape reset links.
- `MAIL_SENDER_FROM`, `MAIL_SENDER_NAME`, and any provider-specific SMTP creds when swapping the logging email sender for a real integration.
- `SECURITY_RATE_LIMIT_FORGOT_PASSWORD_MAX_REQUESTS`, `SECURITY_RATE_LIMIT_FORGOT_PASSWORD_WINDOW` to tune rate limiting.

> The repo root provides `.env.example`. Duplicate it to `.env`, populate secrets, then export them (`export $(grep -v '^#' .env | xargs)`) or rely on automation such as `direnv`, Docker Compose `env_file`, or IDE EnvFile plugins to inject them when running Gradle/Spring Boot.

### Secret Management (Google Cloud)

Recommended Google Secret Manager entries and their mapped environment variables:

| Secret ID | Description | Environment Variable |
|-----------|-------------|----------------------|
| `englishmoon-api-db-password` | Database password | `SPRING_DATASOURCE_PASSWORD` |
| `englishmoon-api-jwt-secret` | Base64-encoded signing secret (256-bit) | `SECURITY_JWT_SECRET` |

Load secrets into the process before starting the app:
```bash
export SPRING_DATASOURCE_PASSWORD="$(gcloud secrets versions access latest --secret=englishmoon-api-db-password)"
export SECURITY_JWT_SECRET="$(gcloud secrets versions access latest --secret=englishmoon-api-jwt-secret)"
./gradlew bootRun
```
CI/CD pipelines should follow the same pattern to prevent secrets from being committed. GitHub Actions jobs should reference repository or environment secrets named `SPRING_DATASOURCE_PASSWORD` and `SECURITY_JWT_SECRET`, populated with the same rotated values (or fed automatically via workload identity) so workflows never read secrets from the repository.

## Contract-First Workflow
1. Update the root `../openapi.yaml`.
2. Run `./gradlew generateOpenApiKotlin`.
3. Commit both the spec and generated server interfaces.
4. Regenerate frontend types from `englishmoon-web` via `yarn types:api`.

## Database & Migrations
- Baseline schema lives in `db/migration/V1__*.sql`.
- Subsequent versioned migrations capture schema changes; seed data goes into higher-numbered migrations.
- For data imports, write a one-off migration (`V2__import.sql`) referencing prepared CSVs or using SQL transforms.
- To drop and rebuild locally: `dropdb englishmoon && createdb englishmoon` (or use Docker container commands), then rerun `./gradlew flywayMigrate`.

### Database Schema Overview

| Table | Key Columns | Purpose |
|-------|-------------|---------|
| `users` | `id` (UUID PK), `email`, `password_hash`, `display_name`, `created_at` | Learner accounts with unique emails. |
| `courses` | `id`, `title`, `summary`, `published_at`, `created_at`, `updated_at` | Course catalog metadata. |
| `lessons` | `id`, `course_id`, `title`, `content`, `order_index`, `created_at` | Lessons linked to courses (`ON DELETE CASCADE`). |
| `enrollments` | `id`, `user_id`, `course_id`, `enrolled_at` | User/course join table with unique constraint per pair. |
| `quizzes` | `id`, `lesson_id`, `title`, `available_at` | Lesson assessments. |
| `quiz_questions` | `id`, `quiz_id`, `prompt`, `answer`, `order_index` | Static quiz questions per quiz. |
| `articles` | `id`, `slug`, `title`, `excerpt`, `image`, `read_time`, `published_on`, timestamps | Marketing/editorial content surfaced to the frontend. |
| `article_sections` | `id`, `article_id`, `order_index`, `content` | Paragraph blocks composing each article (ordered). |
| `refresh_tokens` | `id`, `user_id`, `token_hash`, `issued_at`, `expires_at`, `revoked_at`, `created_at` | Active refresh token registry (indexed by `user_id`, `expires_at`). |

All primary keys use UUID values (generated by the application or database default). Foreign keys enforce referential integrity and cascade deletions for child records.

## Testing Strategy
- Unit + slice tests reside alongside application/infrastructure classes.
- Integration tests (tagged `integration`) spin up ephemeral PostgreSQL via Testcontainers. They will:
  - Reuse the shared container defined with `@Container`.
  - Map dynamic connection properties through `@DynamicPropertySource`.
  - Skip automatically when Docker isn’t accessible, keeping local feedback loops fast.

## Continuous Integration
- `.github/workflows/api-ci.yml` executes on pull requests targeting `develop`.
- Job 1: `ktlintCheck` + `test -PskipIntegrationTests=true`.
- Job 2 (depends on Job 1): `integrationTest` using Testcontainers (Docker-in-Docker on GitHub-hosted runners).
- Extend the workflow with image builds or deployment steps once environments are ready.

## Observability & Hardening Notes
- Structured logging via Spring Boot defaults; instrumenters can plug Micrometer + OTLP within `infra`.
- CORS is locked down to known origins in `SecurityConfig`.
- Add rate limiting, JSON error envelopes, and CSRF token support before moving beyond prototype stage.

## Next Up
- Implement password reset flows + transactional email delivery.
- Add OAuth providers (Google) to complement username/password authentication.
- Extend observability with metrics + tracing exporters for production readiness.
- Provide migration scripts for importing legacy course/article content beyond the seeded samples.
