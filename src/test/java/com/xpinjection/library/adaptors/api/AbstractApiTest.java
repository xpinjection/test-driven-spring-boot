package com.xpinjection.library.adaptors.api;

import com.github.database.rider.spring.api.DBRider;
import com.github.viclovsky.swagger.coverage.FileSystemOutputWriter;
import com.github.viclovsky.swagger.coverage.SwaggerCoverageV3RestAssured;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Path;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ContextConfiguration(initializers = StandaloneApplicationContextInitializer.class)
@DBRider
@Slf4j
@ActiveProfiles("test")
public abstract class AbstractApiTest {
    private static final ApiReports REPORTS = ApiReports.builder()
            .coveragePath(Path.of("target", "api-coverage"))
            .exportedSpecPath("target/api-docs.yaml")
            .changesPath("target/api-diff.md")
            .build();
    private static OpenApi OPEN_API;

    @LocalServerPort
    protected int port;

    @Autowired
    private SwaggerUiConfigParameters swaggerConfig;

    @BeforeEach
    void init() {
        RestAssured.port = port;
        if (OPEN_API == null) {
            REPORTS.clean();
            OPEN_API = new OpenApi(swaggerConfig.getUrls());
            OPEN_API.validate(port, REPORTS.getChangesPath());
            OPEN_API.export(port, REPORTS.getExportedSpecPath());
        }
    }

    protected RequestSpecification given() {
        var apiCoverageWriter = new FileSystemOutputWriter(REPORTS.getCoveragePath());
        return RestAssured.given()
                .filter(new SwaggerCoverageV3RestAssured(apiCoverageWriter));
    }
}
