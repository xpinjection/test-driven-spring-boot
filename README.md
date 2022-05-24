# test-driven-spring-boot

![CI status](https://github.com/xpinjection/test-driven-spring-boot/actions/workflows/maven.yml/badge.svg)

Sample Spring Boot applications written in TDD style.

The easiest way to run application locally is to use _LocalLibraryApplication_ class. It will run all needed dependencies in the Docker containers and configure application to use them automatically.

To enable Actuator endpoints admin profile has to be activated with proper secrets provided:

> _-Dspring.security.user.name=admin -Dspring.security.user.password=xpinjection -Dspring.profiles.active=dev,admin_
