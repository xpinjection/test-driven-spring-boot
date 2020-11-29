package com.xpinjection.springboot.init;

import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;

public class ActiveProfilesChecker implements ApplicationListener<ApplicationPreparedEvent> {
    @Override
    public void onApplicationEvent(ApplicationPreparedEvent event) {
        var environment = event.getApplicationContext().getEnvironment();
        if (environment.getActiveProfiles().length == 0) {
            throw new ApplicationStartedWithoutActiveProfileException();
        }
    }
}
