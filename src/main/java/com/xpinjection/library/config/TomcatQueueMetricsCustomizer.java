package com.xpinjection.library.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.AbstractProtocol;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

@Slf4j
@Component
@RequiredArgsConstructor
public class TomcatQueueMetricsCustomizer implements TomcatConnectorCustomizer,
        ApplicationListener<WebServerInitializedEvent> {
    private final MeterRegistry meterRegistry;
    private final List<Connector> connectors = new ArrayList<>();

    @Override
    public void customize(Connector connector) {
        LOG.info("Got Tomcat connector {}, save it for custom metrics configuration", connector);
        connectors.add(connector);
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        LOG.info("Got Tomcat initialization event, adding custom metrics for all known collectors");
        connectors.forEach(this::addMetrics);
    }

    private void addMetrics(Connector connector) {
        var protocolHandler = connector.getProtocolHandler();
        var connectorName = connector.toString();
        if (protocolHandler instanceof AbstractProtocol<?> abstractProtocol) {
            if (abstractProtocol.getExecutor() instanceof ThreadPoolExecutor executor) {
                var queue = executor.getQueue();

                Gauge.builder("tomcat.executor.queue.size", queue, BlockingQueue::size)
                        .description("Number of incoming HTTP requests waiting in Tomcat executor queue")
                        .baseUnit("requests")
                        .tag("connector", connectorName)
                        .register(meterRegistry);

                Gauge.builder("tomcat.executor.queue.remaining", queue, BlockingQueue::remainingCapacity)
                        .description("Remaining capacity of Tomcat executor queue before rejection")
                        .baseUnit("requests")
                        .tag("connector", connectorName)
                        .register(meterRegistry);

                LOG.info("Tomcat queue gauges registered for connector {}", connectorName);
            } else {
                LOG.warn("Tomcat executor is not ThreadPoolExecutor, got: {}", abstractProtocol.getExecutor().getClass());
            }
        } else {
            LOG.warn("ProtocolHandler is not AbstractProtocol: {}", protocolHandler.getClass());
        }
    }
}