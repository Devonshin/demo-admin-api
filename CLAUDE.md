# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the **Allink Mobile E-Receipt API** (올링크 모바일 전자영수증 API) - a Kotlin-based backend service built with Ktor framework that provides electronic receipt issuance, management, and related functionalities.

## Common Development Commands

### Build and Run
```bash
# Build the project
./gradlew build

# Run the application
./gradlew run

# Build fat JAR with all dependencies
./gradlew buildFatJar

# Build Docker image
./gradlew buildImage

# Run using Docker
./gradlew runDocker
```

### Testing
```bash
# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests "ClassName.methodName"
```

### Environment Configuration
- Use JVM arguments: `-config=application.conf -config=application-{environment}.conf`
- Use VM options: `-DKTOR_ENV={environment}`
- Environments: `local`, `test`, `prod`, `production`

## Architecture Overview

### Layered Architecture
The project follows a **domain-driven layered architecture**:
- **Route Layer**: Request handling and response generation (`*Route.kt`)
- **Service Layer**: Business logic implementation (`*Service.kt`, `*ServiceImpl.kt`)
- **Repository Layer**: Data access and database operations (`*Repository.kt`, `*RepositoryImpl.kt`)
- **Model Layer**: Data models and domain entities (`*Model.kt`)
- **Table Objects**: Database table definitions (Exposed ORM)

### Application Bootstrap
The main application entry point is `Application.kt` with module configuration order:
1. `configureMonitoring()` - Logging and health checks
2. `configureStatusPage()` - Error handling
3. `configureValidation()` - Request validation
4. `configureSerialization()` - JSON serialization
5. `configureDatabases()` - Database connections
6. `configureFrameworks()` - Dependency injection (Koin)
7. `configureHTTP()` - CORS, compression
8. `configureSecurity()` - JWT authentication
9. `configureRouting()` - API route registration
10. `configureOpenApi()` - Swagger documentation

### Domain Structure
Core business domains are organized under `src/main/kotlin/io/allink/receipt/api/domain/`:
- **admin**: Administrator management and role-based access
- **login**: Authentication and JWT token management
- **store**: Store/merchant management and billing
- **user**: End-user management and profiles
- **receipt**: Electronic receipt issuance and management
- **npoint**: Point system and rewards
- **merchant**: Merchant tag and category management
- **agency/bz**: Business agency management
- **file**: File upload and management
- **sns**: SMS verification via AWS SNS
- **code**: System code and enum definitions

### Authentication & Authorization
- **JWT Authentication**: All protected routes require valid JWT tokens
- **Role-based Access Control**: Custom `RoleAuthorizationPlugin` enforces menu-based permissions
- **Role Provider**: Extracts roles from JWT claims and validates against accessible menu paths

### Key Technologies
- **Framework**: Ktor 3.1.2
- **Language**: Kotlin 2.1.20
- **Database**: PostgreSQL with Exposed ORM
- **DI Container**: Koin 3.4.0
- **Authentication**: JWT with custom role authorization
- **Cloud Services**: AWS SNS, DynamoDB, S3
- **API Documentation**: Swagger UI, OpenAPI, ReDoc
- **Testing**: JUnit 5, MockK, AssertJ, Ktor Test

## Key Development Patterns

### Domain Module Pattern
Each domain follows a consistent structure:
- `{Domain}Model.kt` - Data models and table definitions
- `{Domain}Repository.kt` - Data access interface
- `{Domain}RepositoryImpl.kt` - Repository implementation
- `{Domain}Service.kt` - Business logic interface
- `{Domain}ServiceImpl.kt` - Service implementation
- `{Domain}Route.kt` - HTTP route definitions
- `{Domain}Schemes.kt` - Request/response schemas (if needed)

### Dependency Injection with Koin
- All services and repositories are registered in Koin modules
- Use constructor injection with `get()` function in routes
- Repository implementations extend `ExposedRepository` base class

### Configuration Management
- Environment-specific configs in `application-{env}.conf`
- JWT configuration with domain, issuer, audience settings
- Database connection settings with PostgreSQL
- AWS service configuration for SNS, S3, DynamoDB

### Error Handling
- Use `ApiException` for business logic errors
- Global error handling via Status Pages plugin
- Proper HTTP status codes and Korean error messages

## Testing Guidelines

### Test Structure
- Unit tests for individual components
- Integration tests for database operations
- API tests for endpoint validation
- Test files mirror main source structure in `src/test/kotlin/`

### Test Frameworks
- **JUnit 5**: Test execution and lifecycle
- **MockK**: Kotlin-specific mocking
- **AssertJ**: Fluent assertions
- **Ktor Test**: Application testing with `testApplication`
- **Kotlinx Coroutines Test**: Coroutine testing support

### Test Naming Convention
Follow pattern: `Should expected behavior when state under test`
Add @DisplayName annotation and explain detail

## Important Notes

- **Database**: Uses PostgreSQL with R2DBC for reactive database access
- **Port**: Application runs on port 8888 by default
- **Documentation**: Swagger UI available at `/swagger-ui` endpoint
- **Health Check**: KHealth plugin provides health monitoring
- **File Structure**: 2-space indentation, 120-character line limit
- **Commit Messages**: Follow conventional commit format with Korean descriptions
- **Branch Strategy**: `main` (production), `prod` (development), `dev` (development), feature branches