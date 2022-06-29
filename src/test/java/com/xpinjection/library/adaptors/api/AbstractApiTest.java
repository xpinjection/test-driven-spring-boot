package com.xpinjection.library.adaptors.api;

import com.github.database.rider.spring.api.DBRider;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@ContextConfiguration(initializers = StandaloneApplicationContextInitializer.class)
@DBRider
@Slf4j
@ActiveProfiles("test")
public abstract class AbstractApiTest {
    private static final OpenApiValidator OPEN_API_VALIDATOR = new OpenApiValidator();

    @LocalServerPort
    protected int port;

    @BeforeEach
    void init() {
        RestAssured.port = port;
        OPEN_API_VALIDATOR.validate(port);
    }
}
