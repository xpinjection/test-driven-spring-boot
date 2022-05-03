package com.xpinjection.library.adaptors;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class FakeManagementConfig {
    @Bean
    public WebEndpointProperties webEndpoints() {
        return new WebEndpointProperties();
    }
}
