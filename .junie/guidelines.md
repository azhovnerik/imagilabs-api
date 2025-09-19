Imagilabs API – Developer Guidelines

Purpose
A quick, practical guide to get you productive in this Kotlin/Spring Boot service.

Tech Stack

- Language: Kotlin (JDK 21)
- Framework: Spring Boot 3.5 (Web, Security, Validation)
- Persistence: Spring Data JPA, PostgreSQL, Flyway
- Docs: springdoc-openapi (Swagger UI)
- Jobs/locks: ShedLock (JDBC)
- External: Google Sheets API, Mail, JWT
- Testing: JUnit 5, MockK/Mockito, Testcontainers, Cucumber
- Build: Gradle (Kotlin DSL), JaCoCo

Repository Structure (high level)

- src/main/kotlin/com/... Application code
    - domain/ business logic
    - web/ REST controllers
    - storage/ JPA entities and repositories
- src/main/resources
    - application.yml (profiles: default, stage, prod)
    - db/migration Flyway SQL migrations
- src/test/kotlin/com/... unit/integration and BDD support
- src/test/resources/features Cucumber features
- dev/ local dev assets (e.g., postgres volume)
- docker-compose.yaml local PostgreSQL
- build.gradle.kts Gradle config

Prerequisites

- JDK 21
- Docker (for DB and Testcontainers)
- Git + make sure ./gradlew is executable

Configuration

- Database (defaults suitable for local):
    - JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/imagilabs
    - JDBC_DATABASE_USERNAME=imagilabs
    - JDBC_DATABASE_PASSWORD=imagilabs
- Optional integrations via environment variables (see application.yml):
    - SPRING_OPEN_AI_API_KEY, MAIL_*, CORS_ALLOWED_ORIGINS, etc.

Run the Database (local)

- docker compose up -d
    - Uses postgres:14, port 5432, credentials above.
- if you want to run the app, no need to run the database separately. Spring Boot will manage it.

Run the Application

- ./gradlew bootRun
- Or build a jar: ./gradlew build
    - Boot runs on default Spring port unless overridden.
- Swagger UI (if enabled): http://localhost:8080/swagger-ui/index.html

Database Migrations

- Flyway runs automatically on startup using scripts in src/main/resources/db/migration.
- To add a migration: create a new VXX__description.sql file; keep it idempotent and small.

Testing

- Run all tests: ./gradlew test
- Run a single test class: ./gradlew test --tests "com.example.YourTestClass"
- Run a single test method: ./gradlew test --tests "*YourTestClass.yourTestMethod"
- Cucumber (runs via test task). Feature files live under src/test/resources/features.
- Testcontainers: requires Docker running. Tests will manage containers automatically.
- Coverage report: ./gradlew jacocoTestReport (HTML under build/reports/jacoco/test/html).

Executing Scripts/Utilities

- Gradle tasks: ./gradlew tasks to list useful tasks.
- DB queries and utilities may exist under queries/ (manual use only; not run automatically).

Coding & Project Best Practices

- Keep layers clean: controllers (web) -> domain (use cases/services) -> storage (repositories/entities).
- Prefer constructor injection; avoid field injection.
- Validate inputs at boundaries (request DTOs, service methods) using javax/jakarta validation.
- Keep Flyway migrations backward-compatible and review carefully before merge.
- Favor small, focused tests:
    - Unit tests for domain with MockK/Mockito
    - Slice/integration tests with Testcontainers when touching DB
    - Cucumber for behavior specs where appropriate
- Do not commit secrets. Use environment variables and .env files excluded from VCS.
- Keep functions small; name things clearly; prefer immutable data structures when possible.

Troubleshooting

- “Connection refused” to DB: ensure docker compose is up and app uses localhost:5432.
- Tests failing with container issues: confirm Docker is running and has enough resources.
- Migration errors: verify Flyway version filenames and SQL syntax; check current schema history table.

Quick Start

1) ./gradlew bootRun
2) ./gradlew test

Reference

- Key configs: src/main/resources/application.yml
- Build and deps: build.gradle.kts
