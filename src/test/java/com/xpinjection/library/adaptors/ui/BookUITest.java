package com.xpinjection.library.adaptors.ui;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.api.dataset.SeedStrategy;
import com.xpinjection.library.RuntimeDependencies;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.htmlunit.WebClient;
import org.htmlunit.html.DomNode;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.htmlunit.MockMvcWebClientBuilder;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.net.URI;

import static io.restassured.RestAssured.given;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;

/**
 * @author Alimenkou Mikalai
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@ImportTestcontainers(RuntimeDependencies.class)
@DisabledIfSystemProperty(named = "testcontainers.enabled", matches = "false")
public class BookUITest {
    @Autowired
    private WebApplicationContext context;

    private WebClient webClient;

    @BeforeEach
    void init() {
        webClient = MockMvcWebClientBuilder.webAppContextSetup(context)
                .useMockMvcForHosts("books.com", "mylibrary.org")
                .build();
    }

    @Test
    void ifLibraryPageIsRequestedThenItIsReturnedAsValidHtmlWithBlockForBooksList() throws IOException {
        HtmlPage page = webClient.getPage("http://books.com/library.html");
        var booksList = page.getElementsByTagName("li").stream()
                .map(DomNode::asNormalizedText)
                .map(bookName -> StringUtils.substringAfter(bookName, ". "))
                .collect(toList());

        assertThat(booksList).contains("Hibernate in Action, Who cares?",
                "Spring in Action, Who knows?");
    }

    @Test
    @DataSet(value = "default-books.xml", strategy = SeedStrategy.REFRESH)
    void ifBooksAreStoredInDatabaseThenTheyAreAllAvailableOnWebPage() {
        given()
            .accept(MediaType.TEXT_HTML_VALUE)
        .when()
            .get(URI.create("/library.html"))
        .then()
            .statusCode(HttpStatus.SC_OK)
            .contentType(MediaType.TEXT_HTML_VALUE)
            .body(Matchers.allOf(
                    containsString("Spring in Action, <em>Who knows?</em>"),
                    containsString("Hibernate in Action, <em>Who cares?</em>")));
    }
}
