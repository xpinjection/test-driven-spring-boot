# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build (runs all tests)
./mvnw clean package

# Run all tests (requires Docker for Testcontainers)
./mvnw test

# Run a single test class
./mvnw -Dtest=BookServiceTest test

# Run without Docker — skips every Testcontainers-based test
./mvnw verify -Dtestcontainers.enabled=false

# Run with Pact Broker contract verification (off by default)
./mvnw verify -Dpactbroker.enabled=true -Dpact.provider.version=0.1.0 -Dpact.provider.branch=master -Dpact.verifier.publishResults=true
```

**Add `--offline` to any Maven command when dependencies haven't changed** — it is a noticeable speedup.

Test execution notes:
- There is **no Failsafe / separate integration-test phase**. Surefire runs everything matching `**/*Test.java` and `**/*Rules.java` during the `test` phase, with `forkCount=0` (single JVM, shared Spring context cache). `verify` therefore runs the same tests as `test`, just with packaging and the CycloneDX SBOM on top.
- Docker-dependent tests are gated by `@DisabledIfSystemProperty(named = "testcontainers.enabled", matches = "false")`. The property must be explicitly `false` to skip them — absence means enabled.
- The Pact provider test (`BookApiPactTest`) is inversely gated on `pactbroker.enabled=true` and is skipped otherwise.

## Tech Stack

Java 25, Spring Boot 3.5.7, Lombok, PostgreSQL 17 + Flyway, Freemarker (UI), Spring Security, SpringDoc OpenAPI. Observability: Micrometer + Prometheus registry, Micrometer Tracing over OpenTelemetry with the **OTLP** exporter (not Zipkin), Zalando Logbook + logstash-logback-encoder for HTTP/JSON logging. Tests: JUnit 5, AssertJ, Mockito, RestAssured, Testcontainers, Database Rider, ArchUnit, Pact, Spring REST Docs, HtmlUnit, openapi-diff, swagger-coverage.

## Architecture

Hexagonal (Ports & Adapters), **mechanically enforced** by ArchUnit in `HexagonalDesignRules.java` — violations fail the build, so check the rules before restructuring anything.

```
domain/                    → core business entities (currently only Book)
service/                   → service INTERFACES only (must be interfaces named *Service)
  impl/                    → @Service implementations (*ServiceImpl)
  dto/                     → service-layer DTOs
  exception/               → business exceptions
adaptors/
  api/                     → inbound REST: *RestController, CommonErrorHandler, api-only DTOs
  persistence/             → outbound: *Dao (CrudRepository), entity/ subpackage
  ui/                      → outbound: *Controller rendering Freemarker templates
config/                    → Spring config, LibrarySettings, init/ startup listeners
```

**Layer access matrix** (who may access whom — everything else is a violation):

| Layer | May be accessed by |
|---|---|
| `domain` | `service.impl`, `adaptors.persistence` |
| `service` (+ `dto`, `exception`) | `service.impl`, `adaptors.api`, `adaptors.ui`, `config` |
| `adaptors.persistence` | `service.impl` only |
| `service.impl`, `adaptors.api`, `adaptors.ui`, `config` | nobody |

**Naming rules** (enforced): `@Entity` → `..domain..` or `..adaptors.persistence.entity..`; `CrudRepository` → `..adaptors.persistence` + `*Dao`; `@RestController` → `..adaptors.api` + `*RestController`; `@Controller` → `..adaptors.ui` + `*Controller`; `@Service` → `..service.impl` + `*ServiceImpl`; everything directly in `..service` must be an interface named `*Service`.

Note the deliberate asymmetry: `Book` is a domain entity, while `ExpertEntity` is a persistence-adaptor entity. Both are legal — pick the one that matches whether the type is genuinely a domain concept.

**Coding conventions** (enforced by `CodingConventionRules.java`):
- Constructor injection only — no field injection (use Lombok `@AllArgsConstructor`)
- Controllers and services must be stateless: fields must be `private final` **and of interface type**
- `@Service` classes may not access any class whose simple name ends in `Service` (other than the one they implement); `@RestController` classes may not depend on other `@RestController`s
- Throw specific exceptions from `..service.exception..` — never generic ones
- SLF4J only (Lombok `@Slf4j`, logger field is `LOG`) — no `java.util.logging`, no `System.out`/`System.err`
- No deprecated APIs
- Test classes must live in the same package as the class under test

## Adding a New Feature (API-first)

1. Define the endpoint in `src/main/resources/api/spec/v1/library-api.yaml` **first** — the static spec is the source of truth
2. Add a domain entity (or persistence entity) if needed, plus a Flyway migration
3. Define the interface in `..service..`; implement as `*ServiceImpl` in `..service.impl..`
4. Add the adaptor: `*RestController` (API), `*Controller` (UI), or `*Dao` (persistence). Tag REST controllers with SpringDoc `@Tag`/`@Operation` and register new tags in `ApiConfig`
5. Put DTOs in `..service.dto..`, API-only DTOs in `..adaptors.api.dto..`, exceptions in `..service.exception..`
6. Run the ArchUnit rules early: `./mvnw -Dtest='*Rules' test --offline`
7. Add tests at the right level (below)

## Testing Strategy

| Level | Example | Setup |
|---|---|---|
| Service unit | `BookServiceTest` | JUnit 5 + Mockito, no Spring |
| Controller slice | `BookRestControllerIntegrationTest` | `@WebMvcTest` + MockMvc |
| Persistence | `BookDaoTest` extends `AbstractDaoTest` | `@DataJpaTest` + `@DBRider` + Testcontainers |
| Full API | `BookApiTest`, `ExpertApiTest` extend `AbstractApiTest` | `@SpringBootTest(RANDOM_PORT)` + RestAssured + Testcontainers |
| UI | `BookUITest` | HtmlUnit against the running app |
| Contract | `BookApiPactTest` | Pact provider verification against MockMvc |
| Architecture | `*Rules.java` | ArchUnit |

**`AbstractApiTest`** is the base for full API tests. It starts the app on a random port with the `test` profile, imports `RuntimeDependencies` (PostgreSQL 17.6 Testcontainer), enables `@DBRider`, and on the first test method exports/compares the OpenAPI spec. Use its `given()` helper instead of `RestAssured.given()` — it attaches the `SwaggerCoverageV3RestAssured` filter that produces the coverage data.

**Datasets:** Database Rider resolves dataset names relative to `datasets/` on the test classpath, so reference them by bare filename: `@DataSet("default-books.xml")`, not `@DataSet("datasets/default-books.xml")`. Files live in `src/test/resources/datasets/` (XML, YAML, JSON, CSV all present). Use `@ExpectedDataSet` to assert final DB state. `datasets/database.dtd` is **generated** — regenerate it by running `AbstractDaoTest.exportDatabaseStructure()` after a schema change rather than hand-editing.

**Test reports written to `target/`:** `api-coverage/` (swagger coverage), `api-docs.yaml` (exported generated spec), `api-diff.md` (static-vs-generated spec diff).

**Important:** the OpenAPI drift check in `OpenApi.validate()` only **logs a warning** and writes the Markdown diff — it does **not** fail the test. After changing an endpoint, read `target/api-diff.md` (or the `WARN` output) to confirm the static spec and the generated spec still agree.

## API Specification

`src/main/resources/api/spec/v1/library-api.yaml` is served statically (via `spring.web.resources.static-locations`) alongside the SpringDoc-generated `/v3/api-docs.yaml`. Swagger UI exposes both as `static` and `generated`; `AbstractApiTest` diffs them by those names, so don't rename the entries in `springdoc.swagger-ui.urls`. SpringDoc only scans `com.xpinjection.library.adaptors.api`.

## Database Migrations

Flyway migrations live in `src/main/resources/db/migration/` as `V1.XX__description.sql`. Tables are singular: `book`, `expert`, `recommendations`.

**Already-committed migrations are immutable.** A `PreToolUse` hook (`.claude/hooks/block-existing-migrations.sh`) blocks Write/Edit on any migration that exists in git `HEAD` — editing an applied migration breaks Flyway checksums everywhere. New migrations created in the current task are freely editable until committed.

## Configuration & Profiles

**An active profile is mandatory.** `ActiveProfilesChecker` (registered in `META-INF/spring.factories`) throws `ApplicationStartedWithoutActiveProfileException` on `ApplicationPreparedEvent` if no profile is active. Always pass `-Dspring.profiles.active=...`.

| Profile | Purpose |
|---|---|
| `dev` | local development: debug logging, SQL logging, Freemarker cache off, Docker Compose lifecycle on, random `library.size` |
| `admin` | opens Actuator (base path `/admin`), Prometheus export, OTLP tracing, JMX, health details |
| `test` | test overrides (Actuator closed, DB Rider debug logging) |
| `json-logs` | Logbook JSON style + custom logstash JSON encoder |
| `structured-logs` | Spring Boot's built-in ECS structured console logging |
| `graceful-shutdown` | graceful server + task-executor shutdown |
| `perftest` | k6/perf runs against the containerized app |
| `training-run` | AOT/CDS training run in `Dockerfile_optimized` (Flyway off, no JDBC metadata) |

Other configuration facts:
- Actuator access defaults to `none`; only the `admin` profile opens it, secured by HTTP Basic (`ActuatorBasicSecurityConfig`). Credentials come from `spring.security.user.*` — in dev, `admin`/`xpinjection`
- `LibrarySettings` is bound from the `library` prefix and registered via `@EnableConfigurationProperties` on `LibraryApplication`
- Caching is `simple` in-memory with cache name `booksByAuthor`, used **programmatically** through `CacheManager` in `BookServiceImpl` — not via `@Cacheable`
- `BookServiceImpl.findBooksByAuthor` is annotated `@Observed(name = "books.search")`, which drives the `books.search` percentile metrics configured in the `admin` profile
- Logbook logs HTTP request/response bodies but excludes `/admin/**`
- Tracing is disabled by default (`management.tracing.enabled: false`) and turned on by the `admin` profile

## Local Development

Run `LocalLibraryApplication` (in `src/test/java`) rather than `LibraryApplication` — it activates the `dev` profile and starts the PostgreSQL Testcontainer automatically via `RuntimeDependencies`, so no Docker Compose stack is needed.

Full observability stack (PostgreSQL, Jaeger, Loki, Tempo, OTel Collector, Prometheus, Grafana):
```bash
docker compose up
```
Grafana: http://localhost:3000, `admin` / `xpinjection`. The ELK alternative (Elasticsearch + Kibana + OTel Collector) is `compose_elk.yaml`. The app writes to `logs/`, which the OTel Collector tails.

Performance testing: `k6/search-books-test.js` (env vars `BASE_URL`, `SCENARIO`, `TARGET_RPS`, `DURATION`), against the `library-app` compose service under the `perftest` compose profile.

`docs/` holds Backstage catalog descriptors, not developer documentation.