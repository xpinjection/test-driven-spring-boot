package com.xpinjection.library;

import org.springframework.boot.builder.SpringApplicationBuilder;

public class LocalLibraryApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(LibraryApplication.class)
                .initializers(new StandaloneApplicationContextInitializer())
                .profiles("dev")
                .run(args);
    }
}
