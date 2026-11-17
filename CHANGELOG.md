# Changelog

All notable changes to this project from November 2024 to November 2026 are documented here.

The project has no tagged releases (the current version is `0.1.0-SNAPSHOT`), so entries are grouped by month, newest first. The most recent commit is dated 12 August 2026; there were no commits between November 2024 and May 2025.

## August 2026

### Added
- Claude Code configuration: runtime permissions, skills and supporting tools.
- Default `AGENTS.md` and `CLAUDE.md` with guidance for AI coding agents.
- Spec Kit and OpenSpec support. The OpenSpec slash commands were later removed in favour of skills.
- Step-by-step guide for building a Kibana dashboard that monitors the distribution of HTTP request statuses.

### Changed
- Upgraded Pact to 4.6.21 and switched to the Spring 6 provider module (#17).
- Upgraded Testcontainers to 2.0.3.

## February 2026

### Added
- ELK stack (Elasticsearch + Kibana) with an OpenTelemetry Collector configuration (`compose_elk.yaml`).

## November 2025

### Added
- `training-run` profile for the AOT/CDS training run.
- Optimized Docker image (`Dockerfile_optimized`) with CDS and AOT cache.
- Grafana Tempo for traces, in addition to Jaeger.
- `.gitignore` extended.

### Changed
- Upgraded to Java 25, Maven 3.9.11 and Spring Boot 3.5.7.
- Docker images and the GitHub workflow now run on Java 25.
- Lazy initialization is disabled.

## October 2025

### Added
- Observation for the book search method (`books.search` metrics).
- Prometheus and Grafana for metrics collection.
- Metrics for the Tomcat request queue, plus a k6 stress test configuration.
- HTTP request/response logging with Logbook for all logging profiles.
- Structured logs in ECS format, with log collection to ELK.
- Dependency information and documentation for configuration properties.

### Changed
- Logs moved to Grafana Loki. The `dev` profile now uses the OpenTelemetry Collector by default.
- Book caching switched to Spring Cache.
- Docker Compose is used in the `dev` profile. Some default configuration moved into that profile, and PostgreSQL was updated.
- Upgraded to Spring Boot 3.5.6. GitHub Actions moved to Java 24, then to Java 25 in November.
- Adjusted the code to deprecated methods in the Pact integration.

### Fixed
- Book search by author.
- Docker Compose container names.

## June 2025

### Added
- MIT license (#16).

### Changed
- Tests that need Testcontainers can be disabled with a system property (#15).
- Agent instructions file renamed to `AGENTS.md`, and agents use the preinstalled Maven in offline mode.

## May 2025

### Added
- Option to skip Testcontainers-based tests via `-Dtestcontainers.enabled=false` (#13, #14).
- `AGENTS.md` with instructions for coding agents, and a Codex setup script that runs Maven (#12). The script was later removed.

### Changed
- Console output replaced with SLF4J logging (#11).
- Maven wrapper updated.
- README CLI command formatting fixed (#9).

### Fixed
- The library now creates the configured number of books at startup.
- Author validation log message in `ExpertServiceImpl` (#7).
