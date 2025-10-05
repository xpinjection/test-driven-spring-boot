package com.xpinjection.library.adaptors.ui;

import com.xpinjection.library.config.ActuatorBasicSecurityConfig;
import com.xpinjection.library.service.BookService;
import com.xpinjection.library.service.dto.BookDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Alimenkou Mikalai
 */
@WebMvcTest(BookController.class)
@ActiveProfiles("test")
@Import(ActuatorBasicSecurityConfig.class)
@EnableConfigurationProperties(WebEndpointProperties.class)
public class BookControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Test
    void ifBooksExistThenTheyAreRenderedOnTheLibraryPage() throws Exception {
        var books = asList(new BookDto(1L, "First", "author"),
                new BookDto(2L, "Second", "another author"));
        when(bookService.findAllBooks()).thenReturn(books);

        mockMvc.perform(get("/library.html")
                        .accept(MediaType.parseMediaType("text/html;charset=UTF-8")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("text/html;charset=UTF-8")))
                .andExpect(content().string(allOf(
                        containsString("First, <em>author</em>"),
                        containsString("Second, <em>another author</em>")))
                );
    }
}
