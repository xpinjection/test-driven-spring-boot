package com.xpinjection.library.adaptors.api;

import com.xpinjection.library.domain.Books;
import com.xpinjection.library.service.BookService;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;

import static io.restassured.RestAssured.given;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class ExpertApiTest extends AbstractApiTest {
    @Test
    /*@ExportDataSet(format = DataSetFormat.XML, outputName = "target/expert-added.xml",
                includeTables = {"expert", "recommendations"})*/
    public void expertCouldBeAddedWithRecommendations() {
        int id = given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("{\n" +
                    "  \"name\": \"Mikalai\",\n" +
                    "  \"contact\": \"+38099023546\",\n" +
                    "  \"recommendations\": [\"Effective Java by Josh Bloch\"]\n" +
                    "}")
        .when()
            .post("/experts")
        .then()
            .statusCode(HttpStatus.SC_OK)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .extract().body().jsonPath().get("id");

        assertThat(id).isPositive();
    }

    @TestConfiguration
    public static class BookTestContext {
        @Bean
        public CommandLineRunner bookInitializer(BookService bookService) {
            System.out.println("!!! Insert test data");
            return (args) -> bookService.addBooks(
                    Books.fromMap(singletonMap("Effective Java", "Josh Bloch")));
        }
    }
}
