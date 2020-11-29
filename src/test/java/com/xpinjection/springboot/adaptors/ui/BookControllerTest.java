package com.xpinjection.springboot.adaptors.ui;

import com.xpinjection.springboot.adaptors.ui.BookController;
import com.xpinjection.springboot.domain.Book;
import com.xpinjection.springboot.service.BookService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ExtendedModelMap;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Alimenkou Mikalai
 */
@RunWith(MockitoJUnitRunner.class)
public class BookControllerTest {
    @Mock
    private BookService bookService;

    private BookController controller;

    private MockMvc mockMvc;

    private List<Book> books = asList(new Book("First", "author"),
            new Book("Second", "another author"));

    @Before
    public void init() {
        controller = new BookController(bookService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        when(bookService.findAllBooks()).thenReturn(books);
    }

    @Test
    public void allBooksAreAddedToModelForLibraryView() {
        var model = new ExtendedModelMap();
        assertThat(controller.booksPage(model), equalTo("library"));
        assertThat(model.asMap(), hasEntry("books", books));
    }

    @Test
    public void requestForLibraryIsSuccessfullyProcessedWithAvailableBooksList() throws Exception {
        this.mockMvc.perform(get("/library.html"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("books", equalTo(books)))
                .andExpect(forwardedUrl("library"));
    }
}