package com.xpinjection.springboot.web;

import com.github.database.rider.core.DBUnitRule;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.SeedStrategy;
import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;
import java.net.URI;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public class BookControllerRestAssuredSystemTest {
    @Autowired
    private WebApplicationContext context;

    @Rule
    public DBUnitRule dbUnitRule = DBUnitRule.instance("system",
            () -> context.getBean(DataSource.class).getConnection());

    @Test
    @DataSet(value = "default-books.xml", executorId = "system", strategy = SeedStrategy.INSERT)
    public void allBooksFromDatabaseAreAvailableOnWeb() throws Exception {
        given()
            .accept("text/html;charset=UTF-8")
        .when()
            .get(URI.create("/library.html"))
        .then()
            .statusCode(HttpStatus.SC_OK)
            .contentType("text/html;charset=UTF-8")
            .content(allOf(
                containsString("Spring in Action, <em>Craig Walls</em>"),
                containsString("Hibernate in Action, <em>Christian Bauer</em>")));
    }
}
