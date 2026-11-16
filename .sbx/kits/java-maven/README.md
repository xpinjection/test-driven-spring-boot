# `java-maven` sbx kit

A [Docker Sandboxes](https://docs.docker.com/ai/sandboxes/) kit (spec v3, `kind: mixin`) that
adds this repository's pinned toolchain to an agent sandbox: **Eclipse Temurin JDK 25.0.1+8**
and **Apache Maven 3.9.11**.

The built-in `claude` workload (`docker.io/sbx/claude:latest`) is a Docker Hardened Image with
no JDK and no Maven, so without this kit a sandboxed agent cannot compile or test anything here.

## Usage

```bash
sbx run claude . --kit ./.sbx/kits/java-maven/
```

Or create the sandbox without attaching to it:

```bash
sbx create --name library-dev claude . --kit ./.sbx/kits/java-maven/
sbx exec library-dev bash -lc 'java -version; mvn -version'
```

Add the kit to a sandbox that already exists:

```bash
sbx kit add library-dev ./.sbx/kits/java-maven/
```

Override a version for one run:

```bash
sbx run claude . --kit ./.sbx/kits/java-maven/ --kit-arg java-maven.mavenVersion=3.9.12
```

## What it installs

| Path | Contents |
|---|---|
| `/opt/java/openjdk` | Temurin JDK, `JAVA_HOME` points here |
| `/opt/maven` | Apache Maven, `MAVEN_HOME` points here |
| `/usr/local/bin` | symlinks for `java`, `javac`, `jar`, `jshell`, `mvn` |

Both directories are added to `PATH`. The kit downloads Maven from `repo.maven.apache.org`, the
same host `.mvn/wrapper/maven-wrapper.properties` already points the wrapper at, so `mvn` and
`./mvnw` would be the same 3.9.11 — but use `mvn`, see below.

The kit declares runtime egress to Maven Central (`repo.maven.apache.org`, `repo1.maven.org`)
and nothing else, and requests an optional shared `~/.m2` cache so a fresh sandbox does not
re-download the whole Spring Boot dependency tree. The cache is keyed on the kit, survives
sandbox deletion, and is shared by every sandbox composing this kit.

## Two gotchas when the host is Windows

### Use `mvn`, not `./mvnw`

On a Windows checkout with `core.autocrlf=true` — and this repo ships no `.gitattributes` to
override it — `mvnw` is CRLF in the working tree, so a Linux container refuses to execute it:

```
./mvnw: cannot execute: required file not found
```

The kit's `mvn` is the same Maven 3.9.11, so use that. (Adding `mvnw text eol=lf` to a
`.gitattributes` and re-checking the file out would fix the wrapper as well, but that is a
change to the repository rather than to this kit.)

### In a git worktree, skip git-commit-id

A worktree's `.git` is a pointer file holding a host path that does not resolve inside the
container, so git commands fail there — and `spring-boot-starter-parent` binds
`git-commit-id-maven-plugin:revision` to `initialize` with `failOnNoGitDirectory` defaulting to
true. Pass `-Dmaven.gitcommitid.skip=true`, or launch the sandbox against the main checkout
instead of a worktree.

## Testcontainers works — do not skip it

The sandbox has its own Docker engine on `/var/run/docker.sock`, and the agent user is in the
`docker` group, so Testcontainers needs no configuration. A plain `mvn verify` runs the whole
suite — `*DaoTest`, `*ApiTest` and the UI tests included — against real PostgreSQL containers.

`-Dtestcontainers.enabled=false` exists to skip those tests on a machine without Docker. It is
not needed here, and using it would hide failures rather than avoid them.

The engine is the sandbox's own, not the host's: the first run pulls `postgres:17.6` and
`testcontainers/ryuk` into it, and containers it starts do not show up in `docker ps` on the
host. Both the engine and the egress its pulls use come from the `claude` workload and the sbx
runtime — this kit neither declares `dockerd@1` nor grants registry egress, so under a
locked-down host network policy the pulls are what would need allowing, not Maven.

## Why glibc and not the repo's Alpine image

Every `Dockerfile*` in this repository builds on `eclipse-temurin:25.0.1_8-jdk-alpine`, a
musl-libc image. The agent workload is Debian-based, so this kit installs the **glibc** tarball
of the same Temurin release (25.0.1+8). Same JDK build, matching libc.

## Bumping a version

Edit the `default` of the relevant entry under `args:` in
[`java-maven.yaml`](java-maven.yaml). Each arg drives the download URL, the build-time version
assertion and the kit's `provides` entry at once, so there is one place to change and a mismatch
fails the build rather than shipping quietly.

The JDK version is split in two — `jdkVersion` (`25.0.1`) and `jdkBuild` (`8`) — because a
`provides` version may not contain a `+` (SPEC-v3 §5.2), while the Temurin release tag and asset
name both need it.

Keep these in step with the rest of the repo:

| Arg | Also pinned in |
|---|---|
| `jdkVersion` / `jdkBuild` | all four `Dockerfile*` (`eclipse-temurin:<version>_<build>-jdk-alpine`), `.github/workflows/maven.yml`, `pom.xml` `<java.version>` |
| `mavenVersion` | `.mvn/wrapper/maven-wrapper.properties`, `Dockerfile_full` |

## Validating a change

```bash
sbx kit validate ./.sbx/kits/java-maven
```

Downloads happen in the builder, so a version typo surfaces as a failed build rather than a
broken sandbox. The build asserts the installed version of both tools; if stronger integrity is
wanted, pin per-architecture SHA-256 digests from Temurin's published `.sha256.txt` as build
args.
