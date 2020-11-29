package com.xpinjection.springboot.config.init;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

public class ActiveProfileMissedFailureAnalyzer extends AbstractFailureAnalyzer<ApplicationStartedWithoutActiveProfileException> {
    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, ApplicationStartedWithoutActiveProfileException cause) {
        return new FailureAnalysis("Active profile is not specified. " +
                "It means that specific profile related settings are not applied " +
                "and environment is not fully initialized.",
                "Please specify active profiles with spring.profiles.active property, " +
                        "for example -Dspring.profiles.active=dev", rootFailure);
    }
}
