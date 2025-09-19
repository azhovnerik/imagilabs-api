# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the **Imagilabs API**, a Spring Boot 3.x application written in Kotlin that serves as the backend for an
educational platform. The API supports:

- Teacher and student user management
- Classroom management and co-teacher functionality
- Project creation and sharing
- Python code compilation and execution
- AI-powered learning assistance via OpenAI integration
- Lovable integration for advanced features
- Teaching materials and lesson bundles
- Email notifications and analytics

## Development Commands

### Build & Test

- `./gradlew build` - Build the application
- `./gradlew test` - Run all unit tests
- `./gradlew bootRun` - Run the application locally
- `./gradlew clean` - Clean build artifacts

### Database

- `docker-compose up db` - Start PostgreSQL database (available at localhost:5432)
- Database credentials: `imagilabs/imagilabs/imagilabs`

### Code Quality

- `./gradlew check` - Run all checks including tests
- The project uses JaCoCo for code coverage metrics

## Architecture

### Domain-Driven Design Structure

The codebase follows a layered architecture with clear separation between:

- **Domain Layer** (`domain/`): Business logic, use cases, and domain services
- **Storage Layer** (`storage/`): JPA entities and repositories
- **Web Layer** (`web/`): REST controllers and DTOs
- **Configuration** (`config/`): Spring configuration classes

### Key Modules

#### Core Features

- **Authentication & Authorization** (`auth/`, `security/`): JWT-based auth with role-based access
- **User Management**: Teachers (`teachers/`), Students (`students/`), Admins (`admins/`)
- **Classroom Management** (`classrooms/`): Classroom CRUD, student enrollment, co-teacher invitations
- **Project Management** (`projects/`): Code project creation, sharing, and collaboration
- **Python Compiler** (`pythoncompiler/`): Integration with external Python execution service

#### Advanced Features

- **OpenAI Integration** (`openai/`): AI-powered coding assistance with token management
- **Lovable Integration** (`lovable/`): Advanced project features and account management
- **Teaching Materials** (`teachingmaterials/`): Lesson bundles and educational content
- **Analytics & Notifications**: CleverTap integration, email service, Google Sheets export

### Data Layer

- **PostgreSQL** database with Flyway migrations in `src/main/resources/db/`
- **JPA/Hibernate** for ORM with custom audit configuration
- **Testcontainers** for integration testing with real database

### Testing Strategy

- **Unit Tests**: Domain logic and service layer testing
- **Integration Tests**: API endpoint testing with Spring Boot Test
- **Acceptance Tests**: Cucumber BDD tests in `src/test/resources/features/`
- **Test Utilities**: Shared test configuration in `common/` package

### External Integrations

- **Email Service**: AWS SES for production, dev service for local development
- **Python Compiler API**: External service for code execution
- **Google Services**: Sheets API for data export, OAuth for authentication
- **Analytics**: CleverTap for user behavior tracking

## Configuration

### Application Properties

- Main config in `src/main/resources/application.yml`
- Profile-specific configs for `stage` and `prod` environments
- Environment variables for sensitive configuration (database, APIs, etc.)

### Security

- JWT tokens with configurable TTL for web/mobile
- CORS configuration for cross-origin requests
- Role-based access control (Teacher, Student, Admin)

### Database Configuration

- Connection pooling and timezone handling for PostgreSQL
- JPA configuration with optimized settings for educational workload

## Development Notes

### Code Style

- Kotlin with Spring Boot conventions
- Arrow-kt functional programming library for error handling
- Use case pattern for business operations
- Repository pattern for data access

### Key Dependencies

- Spring Boot 3.5.x with Kotlin 2.2.x
- Spring Security for authentication
- Spring Data JPA for persistence
- Spring AI for OpenAI integration
- Testcontainers for integration testing
- Jackson for JSON processing
- PDFBox for document generation

### Environment Setup

- Java 21 required
- PostgreSQL 14+ for database
- Docker for local database setup
- External Python compiler service integration
