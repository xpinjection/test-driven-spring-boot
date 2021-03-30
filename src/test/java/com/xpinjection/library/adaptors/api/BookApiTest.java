package com.xpinjection.library.adaptors.api;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.SeedStrategy;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.http.MediaType;

import java.net.URI;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

public class BookApiTest extends AbstractApiTest {
    @Test
    @DataSet(value = "default-books.xml", strategy = SeedStrategy.REFRESH)
    public void allBooksFromDatabaseAreAvailableOnWeb() {
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
