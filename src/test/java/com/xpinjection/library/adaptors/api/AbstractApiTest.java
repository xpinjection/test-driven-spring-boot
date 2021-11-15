package com.xpinjection.library.adaptors.api;

import com.github.database.rider.spring.api.DBRider;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DBRider
@ActiveProfiles("test")
public abstract class AbstractApiTest {
    @LocalServerPort
    protected int port;

    @BeforeEach
    void init() {
        RestAssured.port = port;
    }
}
