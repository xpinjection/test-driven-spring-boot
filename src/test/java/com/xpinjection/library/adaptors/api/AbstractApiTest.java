package com.xpinjection.library.adaptors.api;

import com.github.database.rider.spring.api.DBRider;
import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DBRider
@ActiveProfiles("test")
public abstract class AbstractApiTest {
    @LocalServerPort
    protected int port;

    @Before
    public void init() {
        RestAssured.port = port;
    }
}
