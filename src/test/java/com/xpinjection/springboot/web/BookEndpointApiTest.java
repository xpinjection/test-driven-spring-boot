package com.xpinjection.springboot.web;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.SeedStrategy;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.net.URI;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

public class BookEndpointApiTest extends AbstractEndpointApiTest {
    @Test
    @DataSet(value = "default-books.xml", executorId = "system", strategy = SeedStrategy.INSERT)
    public void allBooksFromDatabaseAreAvailableOnWeb() throws Exception {
        given()
            .accept(MediaType.TEXT_HTML_VALUE)
        .when()
            .get(URI.create("/library.html"))
        .then()
            .statusCode(HttpStatus.SC_OK)
            .contentType(MediaType.TEXT_HTML_VALUE)
            .body(allOf(
                containsString("Spring in Action, <em>Craig Walls</em>"),
                containsString("Hibernate in Action, <em>Christian Bauer</em>")));
    }
}
