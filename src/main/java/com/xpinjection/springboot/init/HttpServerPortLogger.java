package com.xpinjection.springboot.init;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;

@Slf4j
public class HttpServerPortLogger implements ApplicationListener<ServletWebServerInitializedEvent> {
    @Override
    public void onApplicationEvent(ServletWebServerInitializedEvent event) {
        LOG.info("Embedded web server was started on port: {}", event.getWebServer().getPort());
    }
}
