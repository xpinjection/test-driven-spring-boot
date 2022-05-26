package com.xpinjection.library.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(title = "Library API", description = "Library API", version = "0.1.0"),
        servers = @Server(url = "http://localhost:8080"),
        tags = {
                @Tag(name = "books"), @Tag(name = "experts")
        }
)
@Configuration
public class ApiConfig {
}
