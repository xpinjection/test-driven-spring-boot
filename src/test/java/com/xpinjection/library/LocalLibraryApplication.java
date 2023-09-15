package com.xpinjection.library;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

public class LocalLibraryApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(LibraryApplication.class)
                .sources(RuntimeDependenciesConfiguration.class)
                //.initializers(new StandaloneApplicationContextInitializer())
                .applicationStartup(new BufferingApplicationStartup(2048))
                //.applicationStartup(new FlightRecorderApplicationStartup())
                .profiles("dev")
                .run(args);
    }

    @TestConfiguration
    @ImportTestcontainers(RuntimeDependencies.class)
    public static class RuntimeDependenciesConfiguration {
    }
}
