package com.xpinjection.library.adaptors.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.openapitools.openapidiff.core.OpenApiCompare;
import org.openapitools.openapidiff.core.model.ChangedOpenApi;
import org.openapitools.openapidiff.core.output.ConsoleRender;
import org.openapitools.openapidiff.core.output.MarkdownRender;
import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class OpenApi {
    private static final String HOST = "http://localhost:";

    private final String staticApiPath;
    private final String generatedApiPath;

    public OpenApi(Set<AbstractSwaggerUiConfigProperties.SwaggerUrl> urls) {
        this(getApiPath(urls, "static"), getApiPath(urls, "generated"));
    }

    public void validate(int port, String reportPath) {
        LOG.info("Validate generated API at {} with static OpenAPI specification at {}",
                generatedApiPath, staticApiPath);
        var apiDiff = OpenApiCompare.fromLocations(
                HOST + port + staticApiPath,
                HOST + port + generatedApiPath);
        compareAndReport(apiDiff, reportPath);
    }

    public void export(int port, String targetPath) {
        var targetFile = new File(targetPath);
        LOG.info("Export generated API spec from {} to {}", generatedApiPath, targetFile);
        var specUrl = HOST + port + generatedApiPath;
        export(specUrl, targetFile);
    }

    private static String getApiPath(Set<AbstractSwaggerUiConfigProperties.SwaggerUrl> urls, String name) {
        return urls.stream()
                .filter(api -> name.equals(api.getName()))
                .map(api -> api.getUrl())
                .findFirst().orElseThrow();
    }

    private void compareAndReport(ChangedOpenApi apiDiff, String reportPath) {
        if (apiDiff.isDifferent()) {
            var compatible = apiDiff.isCompatible();
            LOG.warn("Generated API differs from the static OpenAPI specification in {} way",
                    compatible ? "COMPATIBLE" : "INCOMPATIBLE");
        } else {
            LOG.info("Generated API is the same as the static OpenAPI specification");
        }
        generateConsoleReport(apiDiff);
        generateMdReport(apiDiff, reportPath);
    }

    private void generateMdReport(ChangedOpenApi apiDiff, String reportPath) {
        try {
            LOG.info("API changes report in Markdown format is available here: {}", reportPath);
            new MarkdownRender().render(apiDiff, new FileWriter(reportPath, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Can't store API changes report", e);
        }
    }

    private void generateConsoleReport(ChangedOpenApi apiDiff) {
        var report = new ByteArrayOutputStream();
        new ConsoleRender().render(apiDiff, new OutputStreamWriter(report, StandardCharsets.UTF_8));
        LOG.warn(report.toString(StandardCharsets.UTF_8));
    }

    private void export(String specUrl, File targetFile) {
        try {
            FileUtils.copyURLToFile(URI.create(specUrl).toURL(), targetFile);
            LOG.info("API spec exported successfully");
        } catch (IOException e) {
            throw new IllegalStateException("Can't export API spec", e);
        }
    }
}
