package com.xpinjection.library.adaptors.api;

import com.xpinjection.library.domain.dto.BookDto;
import com.xpinjection.library.service.BookService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author Alimenkou Mikalai
 */
@Tag(name = "books")
@Validated
@RestController
@AllArgsConstructor
public class BookRestController {
    private final BookService bookService;

    @GetMapping(path = "/books", produces = MediaType.APPLICATION_JSON_VALUE)
    List<BookDto> findBooksByAuthor(@RequestParam @NotBlank String author) {
        return bookService.findBooksByAuthor(author);
    }
}
