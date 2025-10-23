package com.xpinjection.library.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.HttpLogFormatter;
import org.zalando.logbook.Sink;
import org.zalando.logbook.logstash.LogstashLogbackSink;

@Configuration
public class LogstashLogbookConfig {
    @Bean
    @ConditionalOnProperty(name = "logbook.format.style", havingValue = "json")
    Sink logstashSink(HttpLogFormatter formatter) {
        return new LogstashLogbackSink(formatter);
    }
}
