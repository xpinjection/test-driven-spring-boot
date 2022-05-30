package com.xpinjection.library.adaptors.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xpinjection.library.service.BookService;
import com.xpinjection.library.service.dto.BookDto;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Alimenkou Mikalai
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(BookRestController.class)
@ActiveProfiles("test")
public class BookRestControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    private final List<BookDto> books = asList(new BookDto(1L, "First", "A"),
            new BookDto(2L, "Second", "A"));

    @Test
    void ifBooksAreFoundByAuthorThenTheyAreAllReturned() throws Exception {
        when(bookService.findBooksByAuthor("A")).thenReturn(books);

        var response = mockMvc.perform(get("/books?author=A")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn().getResponse().getContentAsString();

        var foundBooks = new ObjectMapper().readValue(response, new TypeReference<List<BookDto>>(){});
        assertThat(foundBooks).isEqualTo(books);
    }

    @Nested
    class ValidationTests {
        @Test
        void ifAuthorParamIsMissedThenReturnBadRequest() throws Exception {
            mockMvc.perform(get("/books")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void ifAuthorParamIsBlankThenReturnBadRequest() throws Exception {
            mockMvc.perform(get("/books?author= \t ")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }
}