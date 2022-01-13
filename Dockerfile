FROM openjdk:17.0.1-slim as builder
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} library.jar
RUN java -Djarmode=layertools -jar library.jar extract

FROM openjdk:17.0.1-slim
VOLUME /tmp
COPY --from=builder dependencies/ ./
COPY --from=builder snapshot-dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]