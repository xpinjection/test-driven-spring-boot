FROM eclipse-temurin:24.0.2_12-jdk-alpine AS builder
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} library.jar
RUN java -jar -Djarmode=tools library.jar extract --layers --destination library

FROM eclipse-temurin:24.0.2_12-jdk-alpine
COPY --from=builder /library/dependencies/ ./
COPY --from=builder /library/snapshot-dependencies/ ./
COPY --from=builder /library/spring-boot-loader/ ./
COPY --from=builder /library/application/ ./
ENTRYPOINT ["java", "-jar", "library.jar"]