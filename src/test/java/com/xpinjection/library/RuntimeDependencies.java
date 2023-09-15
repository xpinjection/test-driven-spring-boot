package com.xpinjection.library;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;

public interface RuntimeDependencies {
    @ServiceConnection
    PostgreSQLContainer<?> POSTGRE_SQL = new PostgreSQLContainer<>("postgres:11")
            .withDatabaseName("library")
            .withUsername("test")
            .withPassword("test");
}
