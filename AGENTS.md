# Repository Guidelines

These guidelines help contributors work effectively in this Kotlin/Spring Boot API.

## Project Structure & Module Organization
- Source: `src/main/kotlin/com/anahoret/imagilabsapi/...` organized by feature (e.g., `openai`, `classrooms`, `lovable`, `teachingmaterials`, `common`).
- Resources: `src/main/resources` (Flyway migrations in `db/migration`).
- Tests: `src/test/kotlin` (unit, slice, and Cucumber integration) and `src/test/resources`.
- Build tooling: Gradle wrapper (`gradlew`), Kotlin 2.2, Java 21.

## Architecture Overview
- See `CLAUDE.md` and `.junie/guidelines.md` for extended rationale. This service follows layered, feature‑based DDD:
  - Web (`web/`) → Domain (`domain/`) → Storage (`storage/`) → `config/`.
- Feature modules keep use cases/services in `domain`, REST in `web`, and JPA entities/repos in `storage`.
- Error handling: functional style with Arrow `Either`; controllers map errors via `common.web.mapErrors` to consistent API responses.
- Persistence: PostgreSQL with Flyway SQL migrations (`src/main/resources/db/migration`). Auditing via `common.storage.JpaAuditingConfig` and `BaseEntity`.
- Integrations: Spring AI OpenAI (`openai`), external Python compiler (`pythoncompiler`), email services (`AwsEmailService`, `DevEmailService`), analytics/exports (CleverTap, Google Sheets). Scheduling/locks via ShedLock; e.g., `ReplenishTipTokensJob`.
- Quality/tooling: custom Gradle code metrics plugin in `buildSrc` applied in `build.gradle.kts`.

## Build, Test, and Development Commands
- Build: `./gradlew build` — compiles and runs tests, produces artifacts.
- Run locally: `./gradlew bootRun` — starts the API (Spring Boot).
- Tests: `./gradlew test` — runs JUnit 5, MockK/Mockito, and Cucumber.
- Coverage: `./gradlew jacocoTestReport` — see `build/reports/jacoco/test/html/index.html`.
- Database (local): `docker compose up -d db` — Postgres at `localhost:5432` (db/user/pass: `imagilabs`).

## Coding Style & Naming Conventions
- Kotlin style, 4-space indentation, no wildcard imports.
- Names: classes `PascalCase`, functions/vars `camelCase`, constants `UPPER_SNAKE_CASE`.
- Package by feature under `com.anahoret.imagilabsapi.<feature>`; keep web/domain/storage separated where applicable.
- Prefer immutable data, constructor injection, and small focused use cases/services.

## Testing Guidelines
- Frameworks: JUnit 5, Spring Test, MockK/Mockito, Cucumber.
- Place tests mirroring package paths in `src/test/kotlin/...`.
- Naming: `{ClassName}Test.kt` for unit tests; `{Feature}.feature` for Cucumber in `src/test/resources/features`.
- Aim to cover domain and controllers; mock external integrations. Keep DB tests using Testcontainers where present.

## Commit & Pull Request Guidelines
- Commits: imperative mood, concise subject; reference issues (e.g., `Fix: adjust OpenAI validator (#123)`).
- PRs: include scope/summary, screenshots for API docs if useful, and notes on migrations.
- Requirements: passing CI, `./gradlew build` green, tests added/updated, no unrelated formatting churn.

## Security & Configuration Tips
- Local env example: `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/imagilabs`, `SPRING_DATASOURCE_USERNAME=imagilabs`, `SPRING_DATASOURCE_PASSWORD=imagilabs`.
- Avoid committing secrets. Use environment variables for API keys.
- API docs available via SpringDoc at `/swagger-ui/index.html` when running locally.
