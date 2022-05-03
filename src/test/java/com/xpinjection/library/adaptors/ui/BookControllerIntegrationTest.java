package com.xpinjection.library.adaptors.ui;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xpinjection.library.adaptors.FakeManagementConfig;
import com.xpinjection.library.domain.Book;
import com.xpinjection.library.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.htmlunit.MockMvcWebClientBuilder;

import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Alimenkou Mikalai
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(BookController.class)
@Import(FakeManagementConfig.class)
@ActiveProfiles("test")
public class BookControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    private WebClient webClient;

    @MockBean
    private BookService bookService;

    private List<Book> books = asList(new Book("First", "author"),
            new Book("Second", "another author"));

    @BeforeEach
    void init() {
        when(bookService.findAllBooks()).thenReturn(books);
        webClient = MockMvcWebClientBuilder.mockMvcSetup(mockMvc)
                .useMockMvcForHosts("books.com", "mylibrary.org")
        		.build();
    }

    @Test
    void requestForLibraryIsSuccessfullyProcessedWithAvailableBooksList() throws Exception {
        this.mockMvc.perform(get("/library.html")
                .accept(MediaType.parseMediaType("text/html;charset=UTF-8")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("text/html;charset=UTF-8")))
                .andExpect(content().string(allOf(
                        containsString("First, <em>author</em>"),
                        containsString("Second, <em>another author</em>")))
                );
    }

    @Test
    void libraryPageContentIsRenderedAsHtmlWithListOfBooks() throws IOException {
        HtmlPage page = webClient.getPage("http://books.com/library.html");
        var booksList = page.getElementsByTagName("li").stream()
                .map(DomNode::asNormalizedText)
                .collect(toList());

        assertThat(booksList).contains("1. First, author", "2. Second, another author");
    }
}
