package com.xpinjection.library.adaptors.api;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.openapitools.openapidiff.core.OpenApiCompare;
import org.openapitools.openapidiff.core.model.ChangedOpenApi;
import org.openapitools.openapidiff.core.output.ConsoleRender;
import org.openapitools.openapidiff.core.output.MarkdownRender;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class OpenApiValidator {
    private static final String LOCALHOST = "http://localhost:";
    private static final String STATIC_API_PATH = "/v1/library-api.yaml";
    private static final String GENERATED_API_PATH = "/v3/api-docs.yaml";
    private static final String MD_API_CHANGES_REPORT = "target/api-diff.md";

    private boolean validated;

    public void validate(int port) {
        if (validated) {
            return;
        }
        var httpBase = LOCALHOST + port;
        LOG.info("Validate generated API at {} with static OpenAPI specification at {}",
                GENERATED_API_PATH, STATIC_API_PATH);
        var apiDiff = OpenApiCompare.fromLocations(
                httpBase + STATIC_API_PATH,
                httpBase + GENERATED_API_PATH);

        try {
            compareAndReport(apiDiff);
        } finally {
            validated = true;
        }
    }

    private void compareAndReport(ChangedOpenApi apiDiff) {
        if (apiDiff.isDifferent()) {
            var compatible = apiDiff.isCompatible();
            LOG.warn("Generated API differs from the static OpenAPI specification in {} way",
                    compatible ? "COMPATIBLE" : "INCOMPATIBLE");
        } else {
            LOG.info("Generated API is the same as the static OpenAPI specification");
        }
        generateConsoleReport(apiDiff);
        generateMdReport(apiDiff);
    }

    private void generateMdReport(ChangedOpenApi apiDiff) {
        var mdReport = new MarkdownRender().render(apiDiff);
        LOG.info("API changes report in Markdown format is available here: {}", MD_API_CHANGES_REPORT);
        try {
            FileUtils.writeStringToFile(new File(MD_API_CHANGES_REPORT), mdReport, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Can't store API changes report", e);
        }
    }

    private void generateConsoleReport(ChangedOpenApi apiDiff) {
        var consoleReport = new ConsoleRender().render(apiDiff);
        LOG.warn(consoleReport);
    }
}
