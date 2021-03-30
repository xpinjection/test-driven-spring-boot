package com.xpinjection.library;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;

import java.util.Map;
import java.util.stream.Stream;

public class StandaloneApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final PostgreSQLContainer<?> POSTGRE_SQL = new PostgreSQLContainer<>("postgres:11")
            .withDatabaseName("library")
            .withUsername("test")
            .withPassword("test");

    private static Map<String, Object> runDependencies() {
        Startables.deepStart(Stream.of(POSTGRE_SQL)).join();

        return Map.of(
                "spring.datasource.url", POSTGRE_SQL.getJdbcUrl(),
                "spring.datasource.username", POSTGRE_SQL.getUsername(),
                "spring.datasource.password", POSTGRE_SQL.getPassword()
        );
    }

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        var env = context.getEnvironment();
        var dependencies = new MapPropertySource("dependencies", runDependencies());
        env.getPropertySources().addFirst(dependencies);
    }
}
