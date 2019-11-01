package com.xpinjection.springboot.web;

import com.github.database.rider.core.DBUnitRule;
import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractEndpointApiTest {
    @Autowired
    protected WebApplicationContext context;

    @LocalServerPort
    protected int port;

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance("system",
            () -> context.getBean(DataSource.class).getConnection());

    @Before
    public void init() {
        RestAssured.port = port;
    }
}
