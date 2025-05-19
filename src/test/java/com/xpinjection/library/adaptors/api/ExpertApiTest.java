package com.xpinjection.library.adaptors.api;

import com.xpinjection.library.service.BookService;
import com.xpinjection.library.service.dto.Books;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class ExpertApiTest extends AbstractApiTest {
    @Test
    /*@ExportDataSet(format = DataSetFormat.XML, outputName = "target/expert-added.xml",
                includeTables = {"expert", "recommendations"})*/
    public void ifExpertHasValidParamsAndRecommendationsThenItIsStored() {
        int id = given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body("""
                    {
                      "name": "Mikalai",
                      "contact": "+38099023546",
                      "recommendations": ["Effective Java by Josh Bloch"]
                    }""")
        .when()
            .post("/experts")
        .then()
            .statusCode(HttpStatus.SC_OK)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .extract().body().jsonPath().get("id");

        assertThat(id).isPositive();
    }

    @TestConfiguration
    @Slf4j
    public static class BookTestContext {
        @Bean
        public CommandLineRunner bookInitializer(BookService bookService) {
            log.info("Insert test data");
            return (args) -> bookService.addBooks(
                    Books.fromMap(singletonMap("Effective Java", "Josh Bloch")));
        }
    }
}
