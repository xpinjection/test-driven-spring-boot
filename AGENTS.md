# AI Agent Guide

This is a cloud-native Spring Boot microservice following hexagonal architecture principles with strict ArchUnit rules enforcement.

## Project Structure

### Hexagonal Architecture Layers
The project follows a strict hexagonal (ports and adapters) design enforced by ArchUnit tests in `HexagonalDesignRules.java`:

- **Domain** (`..domain..`): Core business entities (e.g., `Book`, `Author`). May only be accessed by service-impl and persistence-adapter.
- **Service** (`..service`, `..service.dto..`, `..service.exception..`): Service interfaces and DTOs. May only be accessed by service-impl, ui-adapter, api-adapter, and config.
- **Service Implementation** (`..service.impl..`): Service implementations annotated with `@Service`, named with `*ServiceImpl` suffix. May not be accessed by any layer.
- **Adaptors**:
  - **API** (`..adaptors.api..`): REST controllers annotated with `@RestController`, named with `*RestController` suffix. May not be accessed by any layer.
  - **UI** (`..adaptors.ui..`): Web controllers annotated with `@Controller`, named with `*Controller` suffix. May not be accessed by any layer.
  - **Persistence** (`..adaptors.persistence..`): Data access layer with `CrudRepository` interfaces named with `*Dao` suffix, and entities in `..adaptors.persistence.entity..`. May only be accessed by service-impl.
- **Config** (`..config..`): Configuration classes. May not be accessed by any layer.

### Coding Conventions (Enforced by ArchUnit)
All conventions are verified in `CodingConventionRules.java`:

- **No field injection**: Use constructor injection only (via Lombok's `@AllArgsConstructor` or explicit constructors)
- **Stateless components**: Controllers and services must have only `private final` fields of interface types
- **No generic exceptions**: Throw specific custom exceptions from `..service.exception..`
- **No java.util.logging**: Use SLF4J/Logback (Lombok's `@Slf4j`)
- **No standard streams**: Don't use System.out/System.err
- **No deprecated APIs**: Don't use deprecated methods/classes
- **Service isolation**: `@Service` classes cannot depend on other service implementations (only interfaces)
- **Controller isolation**: `@RestController` classes cannot depend on other REST controllers
- **Test co-location**: Test classes should reside in the same package as implementation

## API First Development

- OpenAPI specification is the source of truth: `src/main/resources/api/spec/v1/library-api.yaml`
- Controllers implement operations defined in the OpenAPI spec (use `operationId` for reference)
- DTOs in `..service.dto..` should align with OpenAPI schema definitions
- Use SpringDoc annotations (`@Tag`, `@Operation`) to link controllers to spec

## Configuration Management

- Follow Spring Boot externalized configuration standards
- Main config: `src/main/resources/application.yaml`
- Profile-specific configs: `application-{profile}.yaml` (dev, admin, graceful-shutdown, test)
- Custom configuration properties should use `@ConfigurationProperties` (see `LibrarySettings.java`)
- Enable metadata generation for configuration properties with `spring-boot-configuration-processor`

## Database & Persistence

- Database: PostgreSQL with Flyway migrations
- Database setup via Docker Compose (optional, lifecycle-management: none)

## Technology Stack

- **Java 25** with Spring Boot 3.5.7
- **Lombok**: For reducing boilerplate (getters, constructors, logging)
- **Validation**: Jakarta Validation API (`@NotBlank`, `@Validated`)
- **Observability**: Micrometer with Prometheus, OpenTelemetry tracing, Zipkin exporter
- **Security**: Spring Security with Actuator endpoints protected by basic auth
- **Testing**: JUnit 5, AssertJ, REST Assured, Testcontainers, Database Rider, ArchUnit, Pact, Spring REST Docs

## Dev Environment Tips

- If project dependencies didn't change, run all Maven commands with `--offline` flag
- Quick compile: `mvn compile --offline`
- Test naming: `*Test.java` for unit/integration tests, `*Rules.java` for ArchUnit tests

## Testing Instructions

- **Run all tests**: `mvn verify -Dtestcontainers.enabled=false --offline`
- **Run single test class**: `mvn -Dtest="TestClassName" test -Dtestcontainers.enabled=false --offline`
- **ArchUnit rules**: Always run after structural changes to verify architecture compliance
- The commit should pass all tests (including ArchUnit rules) before merge
- Fix any test errors until the whole suite is green

## When Implementing New Features

0. IMPORTANT: Always consult with Context7 to understand how to implement the task in specified technical stack
1. **Check OpenAPI spec first**: Ensure API contract is defined in `library-api.yaml`
2. **Follow hexagonal layers**:
   - Start with domain entities (if needed)
   - Define service interface in `..service..`
   - Implement service in `..service.impl..` with `*ServiceImpl` naming
   - Create adaptor (API: `*RestController`, UI: `*Controller`, Persistence: `*Dao`)
3. **Use constructor injection**: Leverage Lombok's `@AllArgsConstructor`
4. **Add DTOs**: Place in `..service.dto..` package
5. **Create custom exceptions**: Place in `..service.exception..` package
6. **Run ArchUnit tests**: Verify architectural compliance early and often
7. **Write tests**: Unit tests, integration tests, and update OpenAPI diff tests if needed
