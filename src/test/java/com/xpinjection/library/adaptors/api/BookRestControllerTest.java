package com.xpinjection.library.adaptors.api;

import com.xpinjection.library.domain.dto.BookDto;
import com.xpinjection.library.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Alimenkou Mikalai
 */
@ExtendWith(MockitoExtension.class)
public class BookRestControllerTest {
    @Mock
    private BookService bookService;

    private MockMvc mockMvc;

    private final List<BookDto> books = asList(new BookDto(1L,"First", "A"),
            new BookDto(2L, "Second", "A"));

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new BookRestController(bookService))
                .build();
    }

    @Test
    void booksAreReturnedForAuthor() throws Exception {
        when(bookService.findBooksByAuthor("A")).thenReturn(books);
        mockMvc.perform(get("/books?author=A")
                .accept(MediaType.APPLICATION_JSON))
        				.andExpect(status().isOk())
        				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
        				.andExpect(jsonPath("$[0].name").value("First"))
        				.andExpect(jsonPath("$[0].author").value("A"))
        				.andExpect(jsonPath("$[1].name").value("Second"))
        				.andExpect(jsonPath("$[1].author").value("A"));
    }

    @Test
    void ifAuthorParamIsMissedThrowException() throws Exception {
        mockMvc.perform(get("/books")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}