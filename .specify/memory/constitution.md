<!--
SYNC IMPACT REPORT
==================
Version change: (unversioned template) → 1.0.0
Bump rationale: Initial ratification. All placeholder tokens replaced with concrete,
                project-specific governance. MAJOR baseline established.

Modified principles:
  [PRINCIPLE_1_NAME] → I. Hexagonal Architecture (NON-NEGOTIABLE)
  [PRINCIPLE_2_NAME] → II. Naming and Placement Are Enforced Contracts
  [PRINCIPLE_3_NAME] → III. Stateless, Explicit, Interface-Driven Code
  [PRINCIPLE_4_NAME] → IV. API-First Contract Design
  [PRINCIPLE_5_NAME] → V. Test at the Right Level (NON-NEGOTIABLE)
  (added)            → VI. Reproducible, Self-Contained Tests

Added sections:
  Quality Gates and Technology Constraints (was [SECTION_2_NAME])
  Development Workflow (was [SECTION_3_NAME])

Removed sections: none

Follow-up TODOs: none — all placeholders resolved.
-->

# Library Service Constitution

## Core Principles

### I. Hexagonal Architecture (NON-NEGOTIABLE)

The codebase is organized as Ports & Adapters, and the boundaries are machine-checked by
`HexagonalDesignRules` — a violation is a build failure, not a review comment.

- `domain` MUST only be accessed by `service.impl` and `adaptors.persistence`.
- `service` (including `service.dto` and `service.exception`) MUST only be accessed by
  `service.impl`, `adaptors.api`, `adaptors.ui`, and `config`.
- `adaptors.persistence` MUST only be accessed by `service.impl`.
- `service.impl`, `adaptors.api`, `adaptors.ui`, and `config` MUST NOT be accessed by any layer.
- Business logic MUST live in `service.impl`. Adapters translate between the outside world and
  the service layer; they MUST NOT contain domain rules.

**Rationale:** Inbound adapters (REST, UI) and outbound adapters (persistence) are the parts most
likely to be replaced. Keeping every dependency pointed inward means a transport or storage change
never forces a change to business logic. Enforcement is mechanical because architectural intent
that is only documented erodes within months.

### II. Naming and Placement Are Enforced Contracts

Where a class lives and what it is called are part of the design, not a style preference.
`HexagonalDesignRules` fails the build on any deviation:

- `@Entity` → `..domain..` or `..adaptors.persistence.entity..`
- `CrudRepository` → `..adaptors.persistence`, named `*Dao`
- `@RestController` → `..adaptors.api`, named `*RestController`
- `@Controller` → `..adaptors.ui`, named `*Controller`
- `@Service` → `..service.impl`, named `*ServiceImpl`
- Everything directly in `..service` MUST be an interface named `*Service`
- Service DTOs → `..service.dto..`; API-only DTOs → `..adaptors.api.dto..`;
  business exceptions → `..service.exception..`

New code MUST be placed by asking which layer owns the concept, not by convenience. Adding a class
to a package it does not belong to in order to satisfy a compiler error is a violation of this
principle even when the build happens to pass.

**Rationale:** A predictable name-to-location mapping lets any contributor locate a collaborator
without searching, and lets ArchUnit reason about layers structurally rather than by annotation
alone.

### III. Stateless, Explicit, Interface-Driven Code

`CodingConventionRules` enforces the following; each is a build gate:

- Constructor injection only. Field injection is forbidden — use Lombok `@AllArgsConstructor`.
- Controllers and services MUST be stateless: every field MUST be `private final` **and declared
  with an interface type**.
- `@Service` classes MUST NOT access any other class whose simple name ends in `Service`;
  `@RestController` classes MUST NOT depend on other `@RestController`s. Cross-service
  coordination belongs behind a new port, not in a peer call.
- Exceptions thrown MUST be specific types from `..service.exception..`. Generic exceptions
  (`Exception`, `RuntimeException`, `Throwable`) are forbidden.
- Logging MUST use SLF4J via Lombok `@Slf4j` (logger field `LOG`). `java.util.logging`,
  `System.out`, and `System.err` are forbidden.
- Deprecated APIs MUST NOT be used. When a dependency deprecates something in use, migrating is
  part of the upgrade, not a follow-up.

**Rationale:** Statelessness plus interface-typed dependencies is what makes the service layer
unit-testable with plain Mockito and no Spring context. Banning peer-to-peer service and
controller calls prevents the cyclic dependency graphs that make hexagonal boundaries meaningless
in practice.

### IV. API-First Contract Design

The static OpenAPI specification at `src/main/resources/api/spec/v1/library-api.yaml` is the
source of truth for the HTTP contract.

- Any new or changed endpoint MUST be defined in the static spec **before** the controller is
  written.
- REST controllers MUST carry SpringDoc `@Tag` and `@Operation` metadata, and new tags MUST be
  registered in `ApiConfig`.
- The static spec and the SpringDoc-generated spec MUST agree. `OpenApi.validate()` only logs a
  warning and writes `target/api-diff.md` — it does **not** fail the build. Therefore the author of
  an endpoint change MUST read `target/api-diff.md` (or the `WARN` output) and reconcile any drift
  before the change is considered complete. An unexamined diff is an incomplete change.
- The `static` and `generated` entries in `springdoc.swagger-ui.urls` MUST NOT be renamed;
  `AbstractApiTest` diffs the specs by those names.

**Rationale:** Consumers integrate against the published spec, not the implementation. Writing the
spec first forces the contract to be a design decision rather than a by-product of whatever
annotations ended up on a controller. The drift check is deliberately non-fatal, which shifts the
obligation onto the author — this principle makes that obligation explicit.

### V. Test at the Right Level (NON-NEGOTIABLE)

Every change MUST be covered at the cheapest level that actually exercises the behavior, and MUST
NOT be duplicated at more expensive levels:

| Level | Scope | Mechanism |
|---|---|---|
| Service unit | Business logic in isolation | JUnit 5 + Mockito, no Spring |
| Controller slice | Request mapping, validation, error translation | `@WebMvcTest` + MockMvc |
| Persistence | Queries, mappings, migrations | `@DataJpaTest` + `@DBRider` + Testcontainers |
| Full API | End-to-end HTTP behavior | `@SpringBootTest(RANDOM_PORT)` + RestAssured |
| UI | Freemarker rendering and navigation | HtmlUnit |
| Contract | Consumer expectations | Pact provider verification |
| Architecture | Structural rules | ArchUnit `*Rules.java` |

- Business rules MUST be tested as service unit tests. Reaching for a `@SpringBootTest` to cover
  logic that a Mockito test could cover is a violation.
- New endpoints MUST have a full API test extending `AbstractApiTest`, using its `given()` helper
  rather than `RestAssured.given()` so the swagger-coverage filter is applied.
- Test classes MUST reside in the same package as the class under test (enforced by ArchUnit).
- Architectural rule changes MUST be validated early: `./mvnw -Dtest='*Rules' test --offline`.

**Rationale:** The test suite runs in a single JVM with `forkCount=0` and a shared Spring context
cache; every unnecessary context-loading test is paid for by every contributor on every build.
Level discipline keeps the suite fast enough that nobody is tempted to skip it.

### VI. Reproducible, Self-Contained Tests

- Tests MUST NOT depend on developer-machine state, external environments, or execution order.
  Infrastructure comes from Testcontainers (PostgreSQL 17.6 via `RuntimeDependencies`).
- Database state MUST be established with Database Rider datasets and asserted with
  `@ExpectedDataSet` where final state matters. Datasets are referenced by bare filename
  (`@DataSet("default-books.xml")`) because Rider resolves relative to `datasets/`.
- `datasets/database.dtd` is generated. After a schema change it MUST be regenerated via
  `AbstractDaoTest.exportDatabaseStructure()` and MUST NOT be hand-edited.
- Docker-dependent tests MUST be gated by
  `@DisabledIfSystemProperty(named = "testcontainers.enabled", matches = "false")` so the suite
  still runs without Docker.
- Committed Flyway migrations are **immutable**. A schema correction MUST be a new
  `V1.XX__description.sql` migration. Editing an applied migration breaks checksums in every
  environment and is blocked by a `PreToolUse` hook.
- An active Spring profile is mandatory (`ActiveProfilesChecker` fails startup without one).

**Rationale:** A test that passes only on the machine that wrote it is worse than no test — it
converts a real failure signal into noise that the team learns to ignore.

## Quality Gates and Technology Constraints

**Build gates.** `./mvnw clean package` MUST pass before any change is proposed for review. There
is no separate integration-test phase: Surefire runs `**/*Test.java` and `**/*Rules.java` during
the `test` phase, so ArchUnit violations, unit tests, and container-backed tests all fail the same
build. `verify` adds packaging and the CycloneDX SBOM.

**Non-fatal signals that are still obligations.** Two checks report without failing the build and
therefore MUST be inspected manually by the change author:

- `target/api-diff.md` — static vs. generated OpenAPI drift (see Principle IV)
- `target/api-coverage/` — swagger coverage of the API surface

**Technology baseline.** Java 25, Spring Boot 3.5.7, PostgreSQL 17 + Flyway, Lombok, Freemarker,
Spring Security, SpringDoc OpenAPI. Observability is Micrometer + Prometheus with Micrometer
Tracing over OpenTelemetry using the **OTLP** exporter, plus Zalando Logbook and
logstash-logback-encoder. Introducing a new framework in any of these roles requires an amendment
under Governance, not a pull request.

**Configuration.** Runtime knobs MUST be bound through `@ConfigurationProperties`
(`LibrarySettings`, prefix `library`) rather than scattered `@Value` injections. Behavior that
differs per environment MUST be expressed as a Spring profile, not as a conditional in business
code.

## Development Workflow

The sequence for a new feature is fixed:

1. Define the endpoint in `src/main/resources/api/spec/v1/library-api.yaml` (Principle IV).
2. Add the domain or persistence entity, plus a **new** Flyway migration if the schema changes.
3. Declare the interface in `..service..`; implement it as `*ServiceImpl` in `..service.impl..`.
4. Add the adapter — `*RestController`, `*Controller`, or `*Dao` — with SpringDoc metadata for
   REST, and register new tags in `ApiConfig`.
5. Place DTOs and exceptions per Principle II.
6. Run the architecture rules early: `./mvnw -Dtest='*Rules' test --offline`.
7. Add tests at the levels required by Principle V.
8. Run the full build, then read `target/api-diff.md` and the coverage report.

**Review expectations.** A reviewer MUST verify that the change respects the layer matrix, that
tests exist at the correct level rather than merely existing, and that any OpenAPI drift was
reconciled. "The build is green" is necessary but not sufficient, because the drift and coverage
checks do not fail the build.

**Local development.** Run `LocalLibraryApplication` (in `src/test/java`) rather than
`LibraryApplication`; it activates the `dev` profile and starts the PostgreSQL Testcontainer, so no
Compose stack is needed for ordinary work.

## Governance

This constitution supersedes ad-hoc convention, individual preference, and prior undocumented
practice. Where this document and a code comment disagree, this document wins and the comment is a
defect.

**Amendment procedure.** Amendments MUST be proposed as a change to this file, MUST state the
rationale and the migration impact on existing code, and MUST be accompanied by any corresponding
change to the enforcing ArchUnit rules. A principle that cannot be stated as a check or a
reviewable question is not ready to be a principle.

**Versioning policy.** This constitution follows semantic versioning:

- **MAJOR** — a principle is removed or redefined in a way that invalidates existing compliant code
- **MINOR** — a principle or section is added, or existing guidance is materially expanded
- **PATCH** — clarification, wording, or typo fixes with no change in obligation

**Compliance review.** Every pull request MUST be checked against these principles. Deviations MUST
be justified in writing in the pull request and MUST NOT be introduced by disabling or weakening an
ArchUnit rule; suppressing the check instead of fixing the code is itself a violation. If a rule is
genuinely wrong, amend this constitution and the rule together.

**Runtime guidance.** `CLAUDE.md` at the repository root holds the operational detail — commands,
package layout, profiles, testing mechanics — and MUST be kept consistent with this constitution.
When they conflict, this constitution governs and `CLAUDE.md` MUST be corrected.

**Version**: 1.0.0 | **Ratified**: 2026-08-05 | **Last Amended**: 2026-08-05
