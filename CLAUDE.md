# CLAUDE.md - Repository Knowledge Base

> Comprehensive guide for AI assistants working with the test-driven-spring-boot repository

## Project Overview

**Name:** Library Application
**Type:** Sample Spring Boot microservice demonstrating TDD best practices
**Version:** 0.1.0-SNAPSHOT
**Group:** com.xpinjection
**Purpose:** Reference implementation for building production-ready, cloud-native Spring Boot microservices

### Key Characteristics
- Test-Driven Development (TDD) approach
- Hexagonal Architecture (Ports & Adapters)
- Cloud-native with comprehensive observability
- Consumer-driven contract testing
- Performance testing capabilities
- Java 25 with modern optimizations (AOT, CDS)

## Technology Stack

### Core Technologies
- **Java 25** (Eclipse Temurin)
- **Spring Boot 3.5.7** (Spring Framework 6)
- **Maven 3.9.11**
- **PostgreSQL 17.6**

### Key Frameworks & Libraries
- **Spring Boot Starters:** Web, Data JPA, Security, Actuator, Validation, Cache, AOP
- **Persistence:** Flyway, Hibernate/JPA, HikariCP
- **Testing:** JUnit 5, Mockito, AssertJ, REST Assured, Testcontainers, Database Rider, ArchUnit, Pact, HTMLUnit
- **Observability:** Micrometer, Prometheus, Grafana, OpenTelemetry, Jaeger, Tempo, Loki
- **API:** SpringDoc OpenAPI, OpenAPI 3.0.3
- **Utilities:** Lombok, Apache Commons Lang3, Guava, Jackson

## Architecture

### Hexagonal Architecture Pattern

The codebase strictly follows hexagonal architecture enforced by ArchUnit tests in `src/test/java/com/xpinjection/library/HexagonalDesignRules.java`.

**Layer Structure:**

```
┌─────────────────────────────────────────────┐
│           Adapters Layer                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │   API    │  │    UI    │  │Persistence│  │
│  │/api/     │  │/ui/      │  │/persistence│ │
│  └──────────┘  └──────────┘  └──────────┘  │
├─────────────────────────────────────────────┤
│          Service Layer (Interfaces)         │
│              /service/                       │
├─────────────────────────────────────────────┤
│     Service Implementation Layer            │
│           /service/impl/                     │
├─────────────────────────────────────────────┤
│            Domain Layer                      │
│             /domain/                         │
└─────────────────────────────────────────────┘
```

**Package Structure:**
- `com.xpinjection.library.adaptors.api` - REST controllers
- `com.xpinjection.library.adaptors.ui` - Web MVC controllers
- `com.xpinjection.library.adaptors.persistence` - DAOs and persistence entities
- `com.xpinjection.library.service` - Business logic interfaces
- `com.xpinjection.library.service.impl` - Business logic implementations
- `com.xpinjection.library.domain` - Pure domain entities
- `com.xpinjection.library.config` - Spring configuration

**Architectural Rules (Enforced by ArchUnit):**
1. Domain entities must be in `domain/` or `persistence/entity/` packages
2. DAOs must be in persistence adapter with `Dao` suffix
3. REST controllers must be in API adapter with `RestController` suffix
4. Services must be interfaces in service package with `Service` suffix
5. Service implementations must be in `service.impl` with `ServiceImpl` suffix
6. Controllers and services must be stateless with final fields of interface types
7. Adapters should not access each other
8. No field injection allowed
9. No generic exceptions
10. No Java util logging or standard streams

## Domain Model

### Core Entities

**Book** (`domain/Book.java`)
- Domain entity with JPA annotations
- Fields: `id` (Long), `name` (String), `author` (String)
- Sequence-based ID generation

**ExpertEntity** (`adaptors/persistence/entity/ExpertEntity.java`)
- Persistence entity for book experts
- Fields: `id` (Long), `name` (String), `contact` (String), `recommendations` (Set<Book>)
- One-to-many relationship with Book

### Database Schema

**Migrations:** `src/main/resources/db/migration/`
- V1.00 - Book table (id, name, author)
- V1.01 - Expert table and recommendations join table

## API Endpoints

### REST API

Base specification: `src/main/resources/api/spec/v1/library-api.yaml`

**Book Endpoints:**
- `GET /books?author={author}` - Find books by author (cached)
  - Query param: author (required, non-empty, validated)
  - Response: 200 OK with BookDto[]
  - Caching: Results cached by normalized author name

**Expert Endpoints:**
- `POST /experts` - Add new expert with recommendations
  - Request body: CreateExpertDto (validated)
  - Response: 200 OK with NewExpert (contains ID)
  - Error: 400 Bad Request for invalid recommendations

**Web UI:**
- `GET /library.html` - Display all books (Freemarker template)
  - Template: `src/main/resources/templates/library.ftl`

### Actuator Endpoints

Base path: `/admin`

**Public:**
- `/admin/health/**` - Health checks, readiness/liveness

**Admin (requires ROLE_ADMIN):**
- `/admin/prometheus` - Prometheus metrics
- `/admin/env` - Environment properties
- `/admin/info` - Application info
- `/admin/metrics` - Detailed metrics
- All other actuator endpoints

### API Documentation

- **Swagger UI:** `/swagger-ui.html`
- **OpenAPI Spec:** `/v3/api-docs.yaml`

## Testing Strategy

### Test Types

**1. Unit Tests** - Pure unit tests with Mockito
- Location: `src/test/java/com/xpinjection/library/service/`
- Fast execution, no external dependencies

**2. Integration Tests** - Full Spring context with Testcontainers
- Location: `src/test/java/com/xpinjection/library/adaptors/api/`
- Base class: `AbstractApiTest`
- Real PostgreSQL container
- Can be disabled: `-Dtestcontainers.enabled=false`

**3. Data Layer Tests** - Database Rider + DBUnit
- Base class: `AbstractDaoTest`
- Dataset-driven (XML, YAML, JSON, CSV)
- Test data: `src/test/resources/datasets/`

**4. REST API Tests** - REST Assured
- Base class: `AbstractApiTest`
- Full HTTP testing with random ports
- Swagger coverage tracking
- API contract validation

**5. UI Tests** - HTMLUnit
- Freemarker template rendering validation

**6. Architecture Tests** - ArchUnit
- `HexagonalDesignRules` - Enforces architecture
- `CodingConventionRules` - Enforces coding standards

**7. Contract Tests** - Pact
- Location: `src/test/java/com/xpinjection/library/adaptors/api/pact/`
- Provider verification tests
- Enable: `-Dpactbroker.enabled=true`
- Pact Broker: `pact/docker-compose.yaml`

**8. Performance Tests** - k6
- Script: `k6/search-books-test.js`
- Scenarios: smoke, load, ramp, stress
- Thresholds: p95 < 500ms, p99 < 1000ms

### Running Tests

```bash
# All tests
./mvnw verify

# Skip Testcontainers
./mvnw verify -Dtestcontainers.enabled=false

# With Pact integration
./mvnw verify -Dpactbroker.enabled=true

# Performance tests
k6 run k6/search-books-test.js --env RPS=10
```

## Configuration

### Application Profiles

**dev** - Local development
- Docker Compose auto-start
- Random library size (2-10 books)
- SQL logging enabled
- Debug mode

**admin** - Full monitoring
- All actuator endpoints exposed
- Metrics with percentiles
- OpenTelemetry tracing
- Health checks

**perftest** - Performance testing
- Cache disabled
- OTLP tracing to Jaeger
- Minimal HTTP logging
- Database via Docker service

**training-run** - CDS/AOT training
- Docker Compose disabled
- Flyway disabled
- For AOT cache generation

**structured-logs** - ECS format logging

**json-logs** - JSON format for Logbook

**graceful-shutdown** - Graceful server shutdown

### Activating Profiles

```bash
# Local development with admin access
java -jar library.jar \
  -Dspring.profiles.active=dev,admin \
  -Dspring.security.user.name=admin \
  -Dspring.security.user.password=xpinjection

# Performance testing
java -jar library.jar -Dspring.profiles.active=perftest
```

### Key Configuration Files

- `src/main/resources/application.yaml` - Base configuration
- `src/main/resources/application-{profile}.yaml` - Profile-specific
- `src/test/resources/application-test.yaml` - Test configuration

## Local Development

### Easiest Way: LocalLibraryApplication

```java
// Run this class from your IDE
src/test/java/com/xpinjection/library/LocalLibraryApplication.java
```

This automatically:
- Starts PostgreSQL in Docker
- Configures application to use it
- Enables dev profile
- No manual setup needed

### Docker Compose

```bash
# Start all services
docker compose up -d

# Start specific services
docker compose up -d db jaeger prometheus grafana

# View logs
docker compose logs -f library-app

# Stop all
docker compose down
```

### Services Available

- **PostgreSQL:** localhost:5432 (test/test)
- **Jaeger UI:** http://localhost:16686
- **Prometheus:** http://localhost:9090
- **Grafana:** http://localhost:3000 (admin/xpinjection)
- **Loki:** http://localhost:3100
- **Tempo:** http://localhost:3200
- **Application:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **Actuator:** http://localhost:8080/admin (with admin profile)

## Build & Deployment

### Maven Commands

```bash
# Build
./mvnw clean package

# Build without tests
./mvnw clean package -DskipTests

# Run tests only
./mvnw test

# Integration tests
./mvnw verify

# Offline mode (faster when dependencies cached)
./mvnw --offline package
```

### Docker Images

**Standard Layered Image:**
```bash
./mvnw spring-boot:build-image
# or
docker build -t library:latest .
```

**Basic Image (quick builds):**
```bash
docker build -f Dockerfile_basic -t library:basic .
```

**Full Image (build from source):**
```bash
docker build -f Dockerfile_full -t library:full .
```

**Optimized Image (AOT cache):**
```bash
docker build -f Dockerfile_optimized -t library:optimized .
```

The optimized image uses Java 25's AOT capabilities:
- Training run during build generates AOT cache
- Faster startup times at runtime
- Uses `training-run` profile

## Observability

### Three Pillars

**Metrics (Prometheus + Grafana)**
- Custom Tomcat queue metrics (`TomcatQueueMetricsCustomizer`)
- HTTP server request histograms
- Business metrics via `@Observed` annotations
- Percentiles: p90, p95, p99
- Access: http://localhost:9090 (Prometheus), http://localhost:3000 (Grafana)

**Traces (OpenTelemetry + Jaeger/Tempo)**
- Distributed tracing with OTLP
- Automatic Spring instrumentation
- Database query tracing
- 100% sampling in dev
- Access: http://localhost:16686 (Jaeger)

**Logs (Loki + Structured Logging)**
- ECS format support
- Logbook HTTP request/response logging
- Structured arguments with Logstash encoder
- Centralized log aggregation
- Access: http://localhost:3000 (Grafana with Loki datasource)

### Monitoring Configuration

**Metrics Path:** `/admin/prometheus`
**Health Check:** `/admin/health`
**Actuator Base:** `/admin`

**Key Metrics:**
- `http.server.requests` - HTTP request metrics
- `tomcat.executor.queue.size` - Tomcat queue size
- `tomcat.executor.queue.remaining` - Tomcat queue remaining capacity
- `cache.gets` - Cache hit/miss statistics
- Custom business metrics via Observations

## Key Business Logic

### BookService

**Location:** `src/main/java/com/xpinjection/library/service/BookService.java`

**Methods:**
- `addBooks(Books books)` - Add multiple books
- `findBooksByAuthor(String author)` - Search by author (cached, observed)
- `findAllBooks()` - Get all books

**Features:**
- Author name normalization (handles camel case, whitespace)
- Caching with `@Cacheable("booksByAuthor")`
- Micrometer observations for monitoring
- Structured logging

### ExpertService

**Location:** `src/main/java/com/xpinjection/library/service/ExpertService.java`

**Methods:**
- `addExpert(CreateExpertDto expert)` - Create expert with recommendations

**Features:**
- Validates recommended books exist
- Transactional processing
- Throws `InvalidRecommendationException` for invalid books

### Caching Strategy

**Cache Name:** `booksByAuthor`
**Cache Type:** Simple cache (in-memory)
**Cached Method:** `BookService.findBooksByAuthor()`
**Cache Key:** Normalized author name
**Eviction:** On book additions

Can be disabled with profile: `perftest` (for accurate performance testing)

## Security

### Authentication & Authorization

**Basic Authentication:**
- Type: HTTP Basic
- Admin endpoints require `ROLE_ADMIN`
- Health endpoints are public
- CSRF disabled for API

**Configuration:** `src/main/java/com/xpinjection/library/config/ActuatorBasicSecurityConfig.java`

**Credentials (dev/admin profile):**
- Username: admin
- Password: xpinjection

**Session Management:**
- Stateless sessions
- No JSESSIONID cookies

### Active Profile Enforcement

**Class:** `ActiveProfilesChecker`
**Purpose:** Ensures application always starts with an active profile
**Behavior:** Fails fast on startup if no profile specified

## Performance Optimization

### Java 25 Features

**AOT (Ahead-of-Time) Compilation:**
- Training run generates `app.aot` cache
- Runtime uses pre-compiled code
- Significantly faster startup
- Dockerfile: `Dockerfile_optimized`

**CDS (Class Data Sharing):**
- Alternative to AOT
- Shares class metadata across JVM instances

### Database Optimization

**HikariCP Settings:**
- Minimum idle: 1
- Maximum pool size: 10
- Connection test query configured

**Hibernate Settings:**
- Batch inserts/updates enabled
- Order operations for efficiency
- Open-in-view disabled
- Second-level cache disabled

### Tomcat Tuning

**Configuration:**
- Max threads: 30
- Max connections: 1000
- Accept count: 10
- HTTP/2 enabled
- Compression enabled
- Queue size monitoring

## Consumer-Driven Contracts (Pact)

### Setup

**Pact Broker:**
```bash
cd pact
docker compose up -d
```

Access: http://localhost:9292

### Configuration

**Location:** `pom.xml` - Maven Surefire plugin

**System Properties:**
- `pactbroker.enabled` - Enable Pact integration
- `pact.verifier.publishResults` - Publish verification results
- `pact.provider.version` - Application version
- `pact.provider.branch` - Git branch

### Provider Tests

**Location:** `src/test/java/com/xpinjection/library/adaptors/api/pact/BookApiPactTest.java`

**State Management:**
- `@State("author has books")` - Setup test data
- `@State("author does not have books")` - Empty state

**Running:**
```bash
./mvnw verify \
  -Dpactbroker.enabled=true \
  -Dpact.provider.version=0.1.0 \
  -Dpact.provider.branch=main
```

## CI/CD

### GitHub Actions

**Workflow:** `.github/workflows/maven.yml`

**Triggers:**
- Push to master
- Pull requests to master

**Steps:**
1. Checkout code
2. Setup JDK 25 (Eclipse Temurin)
3. Cache Maven dependencies
4. Build with Maven: `mvn -B package`
5. Publish test results

**Badge:** `![CI status](https://github.com/xpinjection/test-driven-spring-boot/actions/workflows/maven.yml/badge.svg)`

## Common Tasks

### Adding a New Entity

1. Create domain entity in `domain/`
2. Create persistence entity in `adaptors/persistence/entity/`
3. Create DAO in `adaptors/persistence/`
4. Create Flyway migration in `src/main/resources/db/migration/`
5. Run architecture tests to verify compliance

### Adding a New API Endpoint

1. Define in OpenAPI spec: `src/main/resources/api/spec/v1/library-api.yaml`
2. Create DTOs in `adaptors/api/dto/`
3. Create service interface in `service/`
4. Implement service in `service/impl/`
5. Create REST controller in `adaptors/api/`
6. Write unit tests for service
7. Write integration tests (REST Assured)
8. Write Pact provider tests
9. Run architecture tests to verify compliance

### Adding a New Test

**Unit Test:**
- Location: Same package as class under test
- Base class: None (pure unit test)
- Dependencies: Mockito, AssertJ

**Integration Test:**
- Location: `src/test/java/com/xpinjection/library/adaptors/`
- Base class: `AbstractApiTest` or `AbstractDaoTest`
- Dependencies: Testcontainers, Database Rider

**Architecture Test:**
- Location: `src/test/java/com/xpinjection/library/`
- Base class: None
- Dependencies: ArchUnit

### Modifying Configuration

1. Determine appropriate profile (dev, admin, perftest, etc.)
2. Modify `src/main/resources/application-{profile}.yaml`
3. Document in `LibrarySettings` if property needs binding
4. Update this CLAUDE.md if significant

### Performance Testing

1. Ensure app runs with `perftest` profile (cache disabled)
2. Start observability stack: `docker compose up -d`
3. Run k6 script:
   ```bash
   k6 run k6/search-books-test.js --env RPS=10 --env SCENARIO=load
   ```
4. View metrics in Grafana: http://localhost:3000
5. Analyze traces in Jaeger: http://localhost:16686

## Troubleshooting

### Application Won't Start

**Issue:** "No active profile set"
**Solution:** Always specify a profile: `-Dspring.profiles.active=dev`

**Issue:** "Cannot connect to database"
**Solution:**
- For local dev: Use `LocalLibraryApplication`
- For manual run: Start PostgreSQL via `docker compose up -d db`

**Issue:** "Port 8080 already in use"
**Solution:** Stop other instances or change port: `-Dserver.port=8081`

### Tests Failing

**Issue:** Testcontainers timing out
**Solution:**
- Ensure Docker is running
- Disable Testcontainers: `-Dtestcontainers.enabled=false`

**Issue:** Architecture tests failing
**Solution:** Check architectural rules in `HexagonalDesignRules.java` - likely a layer violation

**Issue:** Pact tests failing
**Solution:** Ensure Pact Broker is running: `cd pact && docker compose up -d`

### Build Issues

**Issue:** Maven dependencies not downloading
**Solution:**
- Check internet connection
- Remove `~/.m2/repository/com/xpinjection/library`
- Run without offline mode

**Issue:** Build slow
**Solution:** Use offline mode once dependencies cached: `./mvnw --offline package`

## Code Quality Standards

### Enforced Rules (ArchUnit)

1. **No Field Injection** - Use constructor injection
2. **No Generic Exceptions** - Use specific exceptions
3. **No Java Util Logging** - Use SLF4J
4. **No System.out/err** - Use logging
5. **Stateless Controllers** - Final fields only
6. **Stateless Services** - Final fields only
7. **Dependency Inversion** - Fields must be interfaces

### Naming Conventions

- **Controllers:** `*RestController` (REST API), `*Controller` (Web UI)
- **Services:** `*Service` (interface), `*ServiceImpl` (implementation)
- **DAOs:** `*Dao`
- **DTOs:** `*Dto`
- **Tests:** `*Test`, `*Rules`

### Package Rules

- **Domain entities:** `domain/` or `adaptors/persistence/entity/`
- **Services:** `service/` (interfaces), `service/impl/` (implementations)
- **Controllers:** `adaptors/api/` (REST), `adaptors/ui/` (Web)
- **DAOs:** `adaptors/persistence/`

## Important Files Reference

### Source Code
- Main Application: `src/main/java/com/xpinjection/library/LibraryApplication.java`
- Local Dev Runner: `src/test/java/com/xpinjection/library/LocalLibraryApplication.java`
- Architecture Rules: `src/test/java/com/xpinjection/library/HexagonalDesignRules.java`
- Coding Rules: `src/test/java/com/xpinjection/library/CodingConventionRules.java`

### Configuration
- Base Config: `src/main/resources/application.yaml`
- Dev Profile: `src/main/resources/application-dev.yaml`
- Admin Profile: `src/main/resources/application-admin.yaml`
- Perftest Profile: `src/main/resources/application-perftest.yaml`
- Test Config: `src/test/resources/application-test.yaml`
- JUnit Config: `src/test/resources/junit-platform.properties`

### API & Database
- OpenAPI Spec: `src/main/resources/api/spec/v1/library-api.yaml`
- Flyway Migrations: `src/main/resources/db/migration/`
- Freemarker Template: `src/main/resources/templates/library.ftl`

### Build & Deploy
- Maven POM: `pom.xml`
- Maven Wrapper: `mvnw`, `mvnw.cmd`
- Docker Compose: `compose.yaml`
- Dockerfiles: `Dockerfile`, `Dockerfile_basic`, `Dockerfile_full`, `Dockerfile_optimized`

### Observability
- Prometheus Config: `prometheus/prometheus.yml`
- Grafana Datasources: `grafana/datasources.yml`
- Loki Config: `loki/loki-config.yaml`
- Tempo Config: `tempo/tempo.yaml`
- OTEL Collector: `otel/otel-collector-config.yaml`

### Testing
- Test Datasets: `src/test/resources/datasets/`
- Pact Setup: `pact/docker-compose.yaml`
- Performance Tests: `k6/search-books-test.js`

### CI/CD
- GitHub Actions: `.github/workflows/maven.yml`

### Documentation
- README: `README.md`
- Backstage Catalog: `docs/`
- This File: `CLAUDE.md`

## Additional Resources

### Backstage Integration

The repository includes Backstage catalog definitions in `docs/`:
- Team definition
- Domain definition
- System architecture
- Service components
- Database components
- API definitions

This enables integration with developer portals for:
- Service discovery
- Ownership tracking
- Dependency visualization
- API documentation

### Maven Profiles

**default** - Standard build
**training-run** - Special profile for CDS/AOT training

### System Properties Reference

**Testing:**
- `testcontainers.enabled` - Enable/disable Testcontainers
- `pactbroker.enabled` - Enable Pact integration
- `pact.verifier.publishResults` - Publish Pact results
- `pact.provider.version` - Provider version
- `pact.provider.branch` - Provider branch

**Spring:**
- `spring.profiles.active` - Active profiles
- `spring.security.user.name` - Admin username
- `spring.security.user.password` - Admin password
- `spring.context.exit` - Exit strategy (onRefresh for training)

## Getting Help

### Documentation
- Spring Boot: https://docs.spring.io/spring-boot/docs/current/reference/html/
- Spring Data JPA: https://docs.spring.io/spring-data/jpa/docs/current/reference/html/
- Testcontainers: https://www.testcontainers.org/
- Pact: https://docs.pact.io/
- k6: https://k6.io/docs/

### Common Commands Quick Reference

```bash
# Development
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
# or run LocalLibraryApplication from IDE

# Testing
./mvnw test                    # Unit tests
./mvnw verify                  # All tests
./mvnw verify -Dtestcontainers.enabled=false  # Skip integration tests

# Building
./mvnw clean package           # Build JAR
./mvnw spring-boot:build-image # Build Docker image

# Running
java -jar target/library-0.1.0-SNAPSHOT.jar -Dspring.profiles.active=dev

# Docker
docker compose up -d           # Start all services
docker compose down            # Stop all services
docker compose logs -f app     # View app logs

# Performance Testing
k6 run k6/search-books-test.js --env RPS=10 --env SCENARIO=load
```

## Summary

This is an **exemplary Spring Boot microservice** demonstrating:

- **Clean Architecture:** Hexagonal architecture with enforced boundaries
- **Comprehensive Testing:** Multi-layer testing strategy (unit, integration, contract, performance, architecture)
- **Full Observability:** Three pillars (metrics, traces, logs) with modern tools
- **Modern Java:** Java 25 with AOT and CDS optimizations
- **Cloud-Native:** Containerized, observable, resilient
- **Quality Focused:** Architecture tests, contract tests, API compatibility tests
- **Developer Experience:** Local dev with Testcontainers, offline builds, fast feedback

The project serves as an excellent reference for building production-ready microservices with test-driven development principles.
