# syntax=docker/dockerfile:1
# Deliberately the glibc Temurin build, not the Alpine/musl one the repo's
# Dockerfiles use: the agent workload is Debian-based.
FROM debian:trixie-slim AS build

ARG TARGETARCH
ARG JDK_VERSION
ARG JDK_BUILD
ARG MAVEN_VERSION

RUN apt-get update \
 && apt-get install -y --no-install-recommends ca-certificates curl \
 && rm -rf /var/lib/apt/lists/*

# Temurin names its assets by its own arch spelling, not TARGETARCH's, and the
# feature number leads both the repository name and the asset prefix. Each step
# re-reads the version it installed so a bad pin fails the build.
RUN set -eux; \
    case "${TARGETARCH}" in \
      amd64) jdk_arch=x64 ;; \
      arm64) jdk_arch=aarch64 ;; \
      *) echo "unsupported TARGETARCH: ${TARGETARCH}" >&2; exit 1 ;; \
    esac; \
    feature="${JDK_VERSION%%.*}"; \
    mkdir -p /out/opt/java/openjdk; \
    curl -fsSL \
      "https://github.com/adoptium/temurin${feature}-binaries/releases/download/jdk-${JDK_VERSION}%2B${JDK_BUILD}/OpenJDK${feature}U-jdk_${jdk_arch}_linux_hotspot_${JDK_VERSION}_${JDK_BUILD}.tar.gz" \
      | tar -xz -C /out/opt/java/openjdk --strip-components=1; \
    /out/opt/java/openjdk/bin/java -version 2>&1 | grep -qF "${JDK_VERSION}+${JDK_BUILD}"

# Same host .mvn/wrapper/maven-wrapper.properties points the wrapper at, so the
# kit's mvn and the wrapper's agree by construction.
RUN set -eux; \
    mkdir -p /out/opt/maven; \
    curl -fsSL \
      "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
      | tar -xz -C /out/opt/maven --strip-components=1; \
    JAVA_HOME=/out/opt/java/openjdk /out/opt/maven/bin/mvn -version \
      | grep -qF "Apache Maven ${MAVEN_VERSION}"

RUN set -eux; \
    mkdir -p /out/usr/local/bin; \
    for b in java javac jar jshell; do \
      ln -sf "/opt/java/openjdk/bin/${b}" "/out/usr/local/bin/${b}"; \
    done; \
    ln -sf /opt/maven/bin/mvn /out/usr/local/bin/mvn

# Both tarballs preserve the uid of the machine that packed them, which on an
# unknown base may be a real account that could then rewrite these files.
RUN chown -R 0:0 /out

# A mixin ships only its overlay. Nothing under /home: a mixin may not ship
# /home owned by anyone but root (SPEC-v3 §10), and the ~/.m2 cache is a
# runtime mount anyway.
FROM scratch
COPY --from=build /out /

# A mixin's config records only what it adds; PATH elements are appended to the
# composed image's at assembly, so this states the two added directories alone.
ENV JAVA_HOME=/opt/java/openjdk
ENV MAVEN_HOME=/opt/maven
ENV PATH="/opt/java/openjdk/bin:/opt/maven/bin"
