package com.xpinjection.library.adaptors.api;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.SeedStrategy;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;

@DisabledIfSystemProperty(named = "testcontainers.enabled", matches = "false")
public class BookApiTest extends AbstractApiTest {
    @Test
    @DataSet(value = "default-books.xml", strategy = SeedStrategy.REFRESH)
    void ifBooksAreFoundByAuthorThenTheyAreAllReturned() {
        given()
            .queryParam("author", "Craig Walls")
        .when()
            .get("/books")
        .then()
            .statusCode(HttpStatus.SC_OK)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("size()", greaterThanOrEqualTo(1))
            .body("author", everyItem(equalTo("Craig Walls")))
            .body("name", contains("Spring in Action"));
    }
}
