FROM eclipse-temurin:21.0.2_13-jdk-alpine as builder
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} library.jar
RUN java -Djarmode=layertools -jar library.jar extract

FROM eclipse-temurin:21.0.2_13-jdk-alpine
VOLUME /tmp
COPY --from=builder dependencies/ ./
COPY --from=builder snapshot-dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]