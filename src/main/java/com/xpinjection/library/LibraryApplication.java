package com.xpinjection.library;

import com.xpinjection.library.config.LibrarySettings;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(LibrarySettings.class)
public class LibraryApplication {
	public static void main(String[] args) {
        new SpringApplicationBuilder(LibraryApplication.class)
                .applicationStartup(new BufferingApplicationStartup(2048))
                .run(args);
    }
}
