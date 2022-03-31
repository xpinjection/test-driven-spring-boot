FROM openjdk:17.0.2-slim
VOLUME /tmp
COPY target/*.jar library.jar
ENTRYPOINT ["java","-jar","/library.jar"]