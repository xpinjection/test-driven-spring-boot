package com.xpinjection.library;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

public class LocalLibraryApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(LibraryApplication.class)
                .initializers(new StandaloneApplicationContextInitializer())
                .applicationStartup(new BufferingApplicationStartup(2048))
                //.applicationStartup(new FlightRecorderApplicationStartup())
                .profiles("dev")
                .run(args);
    }
}
