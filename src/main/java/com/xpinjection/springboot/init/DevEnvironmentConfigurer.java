package com.xpinjection.springboot.init;

import org.apache.commons.lang3.Validate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

@Order(Integer.MIN_VALUE)
public class DevEnvironmentConfigurer implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.getActiveProfiles().length == 0) {
            System.out.println("No active profile specified, switch to DEVELOPMENT mode by default");
            environment.addActiveProfile("dev");
        }
        if (environment.acceptsProfiles("dev")) {
            Validate.validState(environment.getActiveProfiles().length == 1,
                    "Development profile could not be mixed with other profiles");
            System.out.println("Application is started in DEVELOPMENT mode");
        }
    }
}
