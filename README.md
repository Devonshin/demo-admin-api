# Demo e-receipt-admin-api
## 올링크 모바일 전자영수증 Demo API

## API Docs
Swagger UI : http://localhost:8080/swagger
Document : http://localhost:8080/doc

---

## Stack / 기술 스택
- Language: Kotlin (JVM) 2.1.20
- Runtime: Java 21 (Temurin)
- Framework: Ktor 3.1.2 (Server Netty)
- DI: Koin
- Persistence: Exposed (core, java-time, r2dbc) + PostgreSQL (R2DBC and JDBC driver available)
- Serialization: kotlinx.serialization + Ktor ContentNegotiation(JSON)
- Auth: Ktor Auth + JWT
- Health: KHealth
- API Docs: ktor-openapi, ReDoc, Ktor Swagger UI
- Testing: JUnit 5, MockK, Ktor test host, Testcontainers (PostgreSQL)
- Coverage: JaCoCo
- Build/Package: Gradle (Kotlin DSL), Ktor plugin fatJar
- Container: Docker, docker-compose

Package manager: Gradle Wrapper (./gradlew)

## Entry points / 진입점
- Main class: io.ktor.server.netty.EngineMain (configured in build.gradle.kts)
- Application module: io.allink.receipt.api.ApplicationKt.module
  - Source: src/main/kotlin/io/allink/receipt/api/Application.kt
  - The main() function loads variables from a local .env into System properties, then delegates to EngineMain.
- Default HTTP port: 8080 (Dockerfile exposes 8080; docker-compose maps 8080:8080)

## API Docs / 문서
Configured in src/main/kotlin/io/allink/receipt/api/config/plugin/OpenApi.kt
- OpenAPI JSON: /.allink-api.json
- ReDoc UI: /doc
- Swagger UI: /swagger (serves openapi/documentation.yaml from resources)

Note: The OpenAPI server list includes sample URLs (localhost:8888, dev/prod URLs) for documentation purposes only.

## Requirements / 요구사항
- JDK 21+
- Gradle Wrapper (bundled)
- Docker 24+ and Docker Compose (for containerized runs and Testcontainers)
- PostgreSQL 15+ (if running database locally outside Docker)

## Setup / 초기 설정
1. Clone the repo
2. Create and review a local environment file:
   - There is a sample .env in the root. Replace values before use. Avoid committing real secrets.
   - The application loads .env automatically on startup for local development.
3. Ensure Docker is running if you plan to use docker-compose or run tests that use Testcontainers.

## Run locally / 로컬 실행
- Using Gradle (development mode):
  - ./gradlew run
- Build all:
  - ./gradlew build
- Run tests:
  - ./gradlew test
- Generate coverage reports:
  - ./gradlew jacocoTestReport
- Enforce coverage rules:
  - ./gradlew jacocoTestCoverageVerification

## Features - 사용 플러그인 목록
Below is the list of Ktor features/plugins included in this project (kept from the original README):

| Name                                                                   | Description                                                                        |
|------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| [Routing](https://start.ktor.io/p/routing)                             | Provides a structured routing DSL                                                  |
| [Authentication](https://start.ktor.io/p/auth)                         | Provides extension point for handling the Authorization header                     |
| [Authentication JWT](https://start.ktor.io/p/auth-jwt)                 | Handles JSON Web Token (JWT) bearer authentication scheme                          |
| [CSRF](https://start.ktor.io/p/csrf)                                   | Cross-site request forgery mitigation                                              |
| [DoubleReceive](https://start.ktor.io/p/double-receive)                | Allows ApplicationCall.receive several times                                       |
| [Request Validation](https://start.ktor.io/p/request-validation)       | Adds validation for incoming requests                                              |
| [Server-Sent Events (SSE)](https://start.ktor.io/p/sse)                | Support for server push events                                                     |
| [Status Pages](https://start.ktor.io/p/status-pages)                   | Provides exception handling for routes                                             |
| [CORS](https://start.ktor.io/p/cors)                                   | Enables Cross-Origin Resource Sharing (CORS)                                       |
| [Compression](https://start.ktor.io/p/compression)                     | Compresses responses using encoding algorithms like GZIP                           |
| [Swagger](https://start.ktor.io/p/swagger)                             | Serves Swagger UI for your project                                                 |
| [Call Logging](https://start.ktor.io/p/call-logging)                   | Logs client requests                                                               |
| [Call ID](https://start.ktor.io/p/callid)                              | Allows to identify a request/call.                                                 |
| [KHealth](https://start.ktor.io/p/khealth)                             | A simple and customizable health plugin                                            |
| [Content Negotiation](https://start.ktor.io/p/content-negotiation)     | Provides automatic content conversion according to Content-Type and Accept headers |
| [kotlinx.serialization](https://start.ktor.io/p/kotlinx-serialization) | Handles JSON serialization using kotlinx.serialization library                     |
| [Exposed](https://start.ktor.io/p/exposed)                             | Adds Exposed database to your application                                          |
| [Postgres](https://start.ktor.io/p/postgres)                           | Adds Postgres database to your application                                         |
| [Koin](https://start.ktor.io/p/koin)                                   | Provides dependency injection                                                      |

## Gradle tasks / 빌드 & 실행
Common tasks you’ll use during development:

| Task                               | Description                                                          |
|------------------------------------|----------------------------------------------------------------------|
| `./gradlew test`                   | Run the tests                                                        |
| `./gradlew build`                  | Build everything                                                     |
| `./gradlew fatJar`                 | Build an executable fat JAR with all dependencies included           |
| `./gradlew run`                    | Run the server (Ktor dev runner)                                     |

Note: Task names are provided by the Ktor Gradle plugin 3.x. Some tasks may differ if the plugin version changes.

## Environment variables / 환경 변수
These can be provided via .env (for local), Docker build args, or your deployment environment. The app will ingest .env at startup for local runs.

- KTOR_ENV: runtime profile (e.g., demo, local, staging, production)
- JWT_SECRET: secret key for signing JWTs
- RECEIPT_DATASOURCE_PG_URL: Postgres host or URL (e.g., localhost)
- RECEIPT_DATASOURCE_DB_NAME: database name (e.g., e-receipt)
- DB_PORT: Postgres port (default 5432)
- DB_USER: database username
- RECEIPT_API_USER_DB_PASSWORD: database password
- AWS_ACCESS_KEY_ID / AWS_SECRET_KEY / AWS_REGION: AWS credentials and region
- S3_RECEIPT_ADMIN_BUCKET_NAME: S3 bucket name for the admin app
- RECEIPT_GATE_KOCES_PAY_URL: external payment gateway endpoint
- KOCES_PAY_TOKEN: token for the payment gateway
- JWT_DOMAIN: JWT domain (used for validation) — TODO: document exact usage
- JWT_ISSUER: JWT issuer — TODO: document exact usage

Security note: .env in this repo includes example values. Replace them before use and do not commit secrets.

## Tests / 테스트
- Unit/integration tests: ./gradlew test
- Testcontainers (PostgreSQL) are used for integration tests. Docker must be available for those tests to run.
- Coverage reports: ./gradlew jacocoTestReport (HTML and XML reports under build/reports/jacoco/test)
- Coverage gate: ./gradlew jacocoTestCoverageVerification (min 50% overall, 40% branch per build.gradle.kts)

## Project structure / 프로젝트 구조
High-level layout:

- src/main/kotlin/io/allink/receipt/api
  - Application.kt (main/module)
  - config/plugin (Ktor plugins: monitoring, security, HTTP, OpenAPI, DB, etc.)
  - domain/* (feature domains: admin, merchant, receipt, store, user, etc.)
  - repository/*, util/*, exception/*
- src/main/resources
  - openapi/*, cert/*, logback.xml
- src/test/kotlin/io/allink/receipt/api (mirrors main packages; tests + testsupport)
- Dockerfile, docker-compose.yml
- scripts/ (deployment helper and git commit tools)
- gradle* files and wrapper

## Acknowledgements
This project was originally bootstrapped with the Ktor Project Generator and includes links to Ktor docs and community resources.
- Ktor Documentation: https://ktor.io/docs/home.html
- Ktor GitHub: https://github.com/ktorio/ktor
- Kotlin Slack (request invite): https://surveys.jetbrains.com/s3/kotlin-slack-sign-up

