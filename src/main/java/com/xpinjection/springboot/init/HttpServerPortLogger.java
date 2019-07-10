package com.xpinjection.springboot.init;

import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;

public class HttpServerPortLogger implements ApplicationListener<ServletWebServerInitializedEvent> {
    @Override
    public void onApplicationEvent(ServletWebServerInitializedEvent event) {
        System.out.println("Embedded web server was started on port: " +
                event.getWebServer().getPort());
    }
}
