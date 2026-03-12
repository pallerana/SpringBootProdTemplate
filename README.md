## Account Service – Spring Boot Production Template

This project is a **production-ready Spring Boot template** for an account management API.  
It showcases clean architecture, Java 17 features, strong validation, idempotency, and rich test coverage.

---

### Tech stack

- **Language**: Java 17
- **Framework**: Spring Boot 4 (Web, Data JPA, Validation, Actuator)
- **Database**: H2 in-memory (for local/dev)
- **Build**: Maven (primary), Gradle (alternative)
- **Mapping**: MapStruct
- **Documentation**: springdoc-openapi (Swagger UI)
- **Testing**: JUnit 5, Mockito, Spring Boot test, JaCoCo coverage

---

### Features

- **Account CRUD API**
  - Create, read (single + list with filters), update (PUT), patch (partial update via PATCH), delete.
  - Strong validation on DTOs with custom validators for country and currency codes.

- **Java 17 usage**
  - `Optional`, streams and functional style utilities.
  - Simple `record` for internal error-detail representation.

- **Clean DTO/entity mapping**
  - MapStruct-based `AccountMapper` for all entity ⇄ DTO conversions.
  - `DtoConverter` utility for reusable functional mapping of lists and single objects.

- **Dynamic filtering via JPA Specifications**
  - `AccountSpecifications` builds type-safe dynamic predicates for list endpoints.
  - `AccountRepository` extends `JpaSpecificationExecutor` and uses `findAll(Specification, Pageable)`.

- **Idempotency support**
  - `IdempotencyService` caches responses by idempotency key + HTTP method + path.
  - `AccountController` uses this for create/update/patch endpoints.

- **Internationalization (i18n)**
  - Central `Translator` utility backed by `i18n/messages.properties` and `ValidationMessages.properties`.
  - Validation and error messages resolved via message codes.

- **Error handling**
  - `GlobalExceptionHandler` with consistent `ApiErrorResponseDTO` contract.
  - `ExceptionUtils` centralizes validation error resolution and error response construction.

- **Production-friendly basics**
  - Actuator endpoints enabled.
  - Central `CommonConstants`, DB field constants, and enums for status and ID prefixes.

---

### Project structure

- `src/main/java/com/example/account`
  - `Application.java` – Spring Boot entry point.
  - `config` – configuration (OpenAPI/Swagger, message source, date converters).
  - `constants` – shared constants (`CommonConstants`, DB column constants).
  - `controller` – REST controllers (`AccountController`).
  - `dto`
    - `account.request` – request DTOs for create/update/list.
    - `account.response` – response DTOs for details, list, create/update/delete.
    - `error` – `ApiErrorResponseDTO`.
  - `enums` – enums like `AccountStatusEnum`, `TransactionPrefixEnum`.
  - `exception` – custom exceptions + `GlobalExceptionHandler`.
  - `i18n` – `Translator` utility.
  - `mapper` – MapStruct `AccountMapper`.
  - `model`
    - `BaseEntity` – common audit/status fields.
    - `account` – `AccountEntity`.
    - `IdempotencyKey` – idempotency persistence.
  - `repository`
    - `AccountRepository` – JPA + `JpaSpecificationExecutor`.
    - `IdempotencyKeyRepository`.
    - `AccountSpecifications` – dynamic JPA Specs for account filtering.
  - `resource` – `WelcomeResource` (simple health-style welcome endpoint).
  - `service`
    - `IdempotencyService`.
    - `account` – interfaces for create/update/delete/retrieve/orchestration.
    - `account.impl` – service implementations (business logic + mapping).
  - `util`
    - `AccountUtil` – ID generation with `TransactionPrefixEnum`.
    - `DtoConverter` – functional mapping helpers.
    - `ExceptionUtils` – error processing.
    - `AccountMessages` – reusable message constants.
  - `validation`
    - Custom annotations and validators for country/currency codes and account validation.

- `src/main/resources`
  - `application.yaml` – main Spring Boot configuration.
  - `database/data.sql` & `data.sql` – sample schema/data.
  - `i18n/messages.properties`, `ValidationMessages.properties` – i18n resources.

- `src/test/java/com/example/account`
  - Unit tests for controllers, services, DTOs, utilities, i18n, and exception handling.
  - Integration tests under `integration` for end-to-end API and config behavior.

---

### API overview

- **Base URL**
  - `http://localhost:8080/api/v1/accounts`

- **Endpoints**
  - `POST /api/v1/accounts` – create account (idempotent via `Idempotency-Key` header).
  - `PUT /api/v1/accounts/{accountId}` – full update.
  - `PATCH /api/v1/accounts/{accountId}` – partial update.
  - `GET /api/v1/accounts/{accountId}` – get single account by ID.
  - `GET /api/v1/accounts` – list accounts with optional filters and pagination.
  - `DELETE /api/v1/accounts/{accountId}` – delete account.
  - `GET /api/welcome` – simple welcome endpoint for quick sanity check.

- **OpenAPI / Swagger**
  - Swagger UI: `http://localhost:8080/swagger-ui.html`
  - OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

### Getting started

#### Prerequisites

- Java 17
- Maven (or Gradle if you prefer the Gradle build)

#### Clone the repository

- `<your own repository URL here>`

#### Build and run (Maven)

- Package:
  - `mvn clean package`
- Run:
  - `java -jar target/account-service-1.0-SNAPSHOT.jar`

#### Build and run (Gradle)

- Package and run:
  - `./gradlew bootRun`

#### Test that the app is running

- Welcome endpoint:
  - `curl -X GET http://localhost:8080/api/welcome`
- Example accounts request:
  - `curl -X GET http://localhost:8080/api/v1/accounts`

---

### H2 configuration

- Console: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

---

### Testing & coverage

- Run unit + integration tests (Maven):
  - `mvn test`
- JaCoCo report:
  - Generated under `target/site/jacoco/index.html`

The template is intended as a solid starting point for real-world APIs: you can change the domain from “accounts” to any other aggregate while preserving the structure, patterns, and tooling. 
