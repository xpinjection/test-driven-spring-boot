package com.xpinjection.library.adaptors.ui;

import com.xpinjection.library.service.BookService;
import com.xpinjection.library.service.dto.BookDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ExtendedModelMap;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Alimenkou Mikalai
 */
@ExtendWith(MockitoExtension.class)
public class BookControllerTest {
    @Mock
    private BookService bookService;

    private BookController controller;

    private MockMvc mockMvc;

    private final List<BookDto> books = asList(new BookDto(1L, "First", "author"),
            new BookDto(2L, "Second", "another author"));

    @BeforeEach
    void init() {
        controller = new BookController(bookService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        when(bookService.findAllBooks()).thenReturn(books);
    }

    @Test
    void allBooksAreAddedToModelForLibraryView() {
        var model = new ExtendedModelMap();
        assertThat(controller.booksPage(model)).isEqualTo("library");
        assertThat(model.asMap()).containsEntry("books", books);
    }

    @Test
    void requestForLibraryIsSuccessfullyProcessedWithAvailableBooksList() throws Exception {
        this.mockMvc.perform(get("/library.html"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("books", equalTo(books)))
                .andExpect(forwardedUrl("library"));
    }
}