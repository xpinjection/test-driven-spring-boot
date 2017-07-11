package com.xpinjection.springboot.init;

import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationListener;

public class HttpServerPortLogger implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {
    @Override
    public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
        System.out.println("Embedded web server was started on port: " +
                event.getEmbeddedServletContainer().getPort());
    }
}
