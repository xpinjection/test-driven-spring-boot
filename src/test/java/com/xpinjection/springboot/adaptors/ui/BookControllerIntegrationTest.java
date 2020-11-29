package com.xpinjection.springboot.adaptors.ui;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.xpinjection.springboot.adaptors.ui.BookController;
import com.xpinjection.springboot.domain.Book;
import com.xpinjection.springboot.service.BookService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.htmlunit.MockMvcWebClientBuilder;

import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Alimenkou Mikalai
 */
@RunWith(SpringRunner.class)
@WebMvcTest(BookController.class)
@ActiveProfiles("test")
public class BookControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    private WebClient webClient;

    @MockBean
    private BookService bookService;

    private List<Book> books = asList(new Book("First", "author"),
            new Book("Second", "another author"));

    @Before
    public void init() {
        when(bookService.findAllBooks()).thenReturn(books);
        webClient = MockMvcWebClientBuilder.mockMvcSetup(mockMvc)
                .useMockMvcForHosts("books.com", "mylibrary.org")
        		.build();
    }

    @Test
    public void requestForLibraryIsSuccessfullyProcessedWithAvailableBooksList() throws Exception {
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
    public void libraryPageContentIsRenderedAsHtmlWithListOfBooks() throws IOException {
        HtmlPage page = webClient.getPage("http://books.com/library.html");
        var booksList = page.getElementsByTagName("li").stream()
                .map(DomNode::asText)
                .collect(toList());

        assertThat(booksList, hasItems("1. First, author", "2. Second, another author"));
    }
}
